package campus.story;

import campus.art.Backgrounds;
import campus.art.Figures;
import campus.fx.Music;
import campus.fx.Sfx;
import campus.lesson.LessonLibrary;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 檢查 {@code story/scripts/} 裡真正的劇本：用到的名稱都存在，而且每一種選法都播得到章末。 */
class ScriptContentTest {

    /** 每章章末筆記本上該寫的字。 */
    private static final Map<String, String> NOTES = Map.of("ch01", "Day 1：教會梁同學什麼是變數。");

    private static StoryLibrary library;

    @BeforeAll
    static void load() throws IOException {
        library = StoryLibrary.load(Path.of("..", "story", "scripts"));
    }

    private record Located(String file, Op.Cmd cmd) {
        String problem(String message) {
            return file + " 第 " + cmd.line() + " 行：" + message;
        }
    }

    private static List<Located> commands(String... names) {
        Set<String> wanted = Set.of(names);
        List<Located> found = new ArrayList<>();
        for (Chapter ch : library.chapters()) {
            for (ScriptFile f : List.of(ch.common(), ch.senpai(), ch.kouhai())) {
                for (ScriptFile.Scene scene : f.scenes()) {
                    for (Op op : scene.ops()) {
                        if (op instanceof Op.Cmd c && wanted.contains(c.name())) {
                            found.add(new Located(f.name(), c));
                        }
                    }
                }
            }
        }
        return found;
    }

    private static void assertNoProblems(List<String> problems) {
        assertTrue(problems.isEmpty(), () -> String.join("\n", problems));
    }

    @Test
    void everyLessonExists() throws IOException {
        LessonLibrary lessons = LessonLibrary.load(Path.of("lessons"));
        List<String> problems = new ArrayList<>();
        for (Located l : commands("程式")) {
            if (!lessons.lessons().containsKey(l.cmd().arg(0))) {
                problems.add(l.problem("沒有程式題 " + l.cmd().arg(0)));
            }
        }
        assertNoProblems(problems);
    }

    @Test
    void everyStagingNameIsKnown() {
        List<String> problems = new ArrayList<>();
        for (Located l : commands("背景", "登場", "表情", "退場", "音樂", "音效", "環境", "特效")) {
            Op.Cmd c = l.cmd();
            String problem = switch (c.name()) {
                case "背景" -> Backgrounds.known(c.arg(0), c.arg(1)) ? null : "沒有背景 " + String.join(" ", c.args());
                case "登場" -> firstProblem(person(c.arg(0)), expression(c.arg(2)));
                case "表情" -> firstProblem(person(c.arg(0)), expression(c.arg(1)));
                case "退場" -> c.arg(0).equals("全部") ? null : person(c.arg(0));
                case "音樂" -> c.arg(0).equals("停") || Music.Track.byScriptName(c.arg(0)).isPresent()
                        ? null : "沒有曲子 " + c.arg(0);
                case "音效" -> Sfx.Sound.byScriptName(c.arg(0)).isPresent() ? null : "沒有音效 " + c.arg(0);
                case "環境" -> c.args().stream()
                        .filter(a -> !a.equals("停") && Sfx.Ambience.byScriptName(a).isEmpty())
                        .findFirst().map(a -> "沒有環境音 " + a).orElse(null);
                case "特效" -> !Effects.known(c.arg(0)) ? "沒有特效 " + c.arg(0)
                        : c.arg(1) != null && !c.arg(1).equals("關") ? "特效後面只能接「關」" : null;
                default -> null;
            };
            if (problem != null) {
                problems.add(l.problem(problem));
            }
        }
        assertNoProblems(problems);
    }

    private static String person(String name) {
        return Cast.find(name).map(Cast.Person::id).filter(Figures.PEOPLE::contains).isPresent()
                ? null : "畫面上沒有這個角色：" + name;
    }

    /** 沒寫表情就是一般。 */
    private static String expression(String e) {
        return e == null || Figures.EXPRESSIONS.contains(e) ? null : "沒有表情 " + e;
    }

    private static String firstProblem(String a, String b) {
        return a != null ? a : b;
    }

    @Test
    void everyDocumentExists() {
        List<String> problems = new ArrayList<>();
        for (Located l : commands("文件", "附件")) {
            String id = l.cmd().name().equals("文件") ? l.cmd().arg(0) : l.cmd().arg(1);
            if (!library.docs().containsKey(id)) {
                problems.add(l.problem("docs/ 裡沒有 " + id + ".md"));
            }
        }
        assertNoProblems(problems);
    }

    /** 自動往下播；選項照 {@code picks} 選，選完了又碰到選項就停在那裡，記下有幾個選項可以選。 */
    private static final class Explorer implements StoryRunner.Stage {
        private final Deque<Integer> picks;
        int waitingOptions = -1;
        String chapterEnd;

        Explorer(List<Integer> picks) {
            this.picks = new ArrayDeque<>(picks);
        }

        @Override
        public void line(Op.Line line, boolean read, Runnable next) {
            next.run();
        }

        @Override
        public void menu(Op.Menu menu, IntConsumer pick) {
            if (picks.isEmpty()) {
                waitingOptions = menu.options().size();
            } else {
                pick.accept(picks.removeFirst());
            }
        }

        @Override
        public void chose(Op.Option option) {
        }

        @Override
        public void command(Op.Cmd cmd, Runnable next) {
            next.run();
        }

        @Override
        public void chapterEnd(String note) {
            chapterEnd = note;
        }

        @Override
        public void checkpoint() {
        }
    }

    @Test
    void everyChoiceSequenceReachesTheChapterEnd() {
        for (Chapter ch : library.chapters()) {
            for (Route route : Route.values()) {
                int paths = 0;
                int minAp = Integer.MAX_VALUE, maxAp = Integer.MIN_VALUE;
                int minTp = Integer.MAX_VALUE, maxTp = Integer.MIN_VALUE;
                Deque<List<Integer>> queue = new ArrayDeque<>();
                queue.add(List.of());
                while (!queue.isEmpty()) {
                    List<Integer> picks = queue.removeFirst();
                    StoryState state = new StoryState();
                    state.begin(ch, route, false);
                    Explorer stage = new Explorer(picks);
                    new StoryRunner(ch, route, state, new HashSet<>(), stage).start();
                    if (stage.waitingOptions >= 0) {
                        for (int i = 0; i < stage.waitingOptions; i++) {
                            List<Integer> longer = new ArrayList<>(picks);
                            longer.add(i);
                            queue.addLast(longer);
                        }
                        continue;
                    }
                    String path = ch.id() + " " + route.label + " 選法 " + picks;
                    assertNotNull(stage.chapterEnd, path + " 沒有播到 @章末");
                    if (NOTES.containsKey(ch.id())) {
                        assertEquals(NOTES.get(ch.id()), stage.chapterEnd, path);
                    }
                    paths++;
                    minAp = Math.min(minAp, state.ap());
                    maxAp = Math.max(maxAp, state.ap());
                    minTp = Math.min(minTp, state.tp());
                    maxTp = Math.max(maxTp, state.tp());
                }
                System.out.printf("%s %s：%d 種選法，AP %d～%d，TP %d～%d%n",
                        ch.id(), route.label, paths, minAp, maxAp, minTp, maxTp);
                String where = ch.id() + " " + route.label;
                assertTrue(minAp >= 10 && maxAp <= 100, where + " 的 AP 超出 10～100：" + minAp + "～" + maxAp);
                assertTrue(minTp >= 10 && maxTp <= 100, where + " 的 TP 超出 10～100：" + minTp + "～" + maxTp);
            }
        }
    }
}
