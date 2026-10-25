package campus.story;

import campus.art.Backgrounds;
import campus.art.Figures;
import campus.fx.Music;
import campus.fx.Sfx;
import campus.lesson.LessonLibrary;
import campus.ui.SvgLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 檢查 {@code story/scripts/} 裡真正的劇本：用到的名稱都存在，而且每一種選法都播得到章末。 */
class ScriptContentTest {

    /** 每章每個視角章末筆記本上該寫的字，鍵是視角劇本的檔名。 */
    private static final Map<String, String> NOTES = Map.of(
            "ch01-senpai.md", "Day 1：教會梁同學什麼是變數。",
            "ch01-kouhai.md", "Day 1：變數是盒子。還有，學長的字很醜。");

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

    @Test
    void everyDocumentImageLoads() throws IOException {
        Pattern image = Pattern.compile("(?m)^!\\[[^\\]]*]\\(([^)]+)\\)\\s*$");
        List<String> problems = new ArrayList<>();
        for (Doc doc : library.docs().values()) {
            for (String page : doc.pages()) {
                Matcher m = image.matcher(page);
                while (m.find()) {
                    Path file = library.docDir().resolve(m.group(1).strip());
                    try {
                        SvgLoader.load(file);
                    } catch (IOException | IllegalArgumentException e) {
                        problems.add("docs/" + doc.id() + ".md 的圖 " + m.group(1) + "：" + e.getMessage());
                    }
                }
            }
        }
        assertNoProblems(problems);
    }

    @Test
    void contentsListEveryChapterButOnlyWrittenOnesArePlayable() {
        List<Integer> numbers = library.contents().stream().map(StoryLibrary.Listing::number).toList();
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), numbers);
        List<Chapter> written = library.contents().stream().map(StoryLibrary.Listing::chapter)
                .filter(Objects::nonNull).toList();
        assertEquals(written, library.chapters());
    }

    /** 自動往下播；選項照 {@code picks} 選，選完了又碰到選項就停在那裡，記下有幾個選項可以選。 */
    private static final class Explorer implements StoryRunner.Stage {
        private final Deque<Integer> picks;
        private final StoryState state;
        int waitingOptions = -1;
        String chapterEnd;
        /** 上一個停點之後播過台詞，還沒碰到 {@code @簡介}。 */
        private boolean untold;
        /** 前面缺簡介的停點：玩家按「跳過」停在這裡時，跳過的那段沒有簡介可看。 */
        final Set<String> unsummarized = new LinkedHashSet<>();

        Explorer(List<Integer> picks, StoryState state) {
            this.picks = new ArrayDeque<>(picks);
            this.state = state;
        }

        /** 選項、程式題、章末是「跳過」會停下來的地方，播放位置正停在那一行。 */
        private void stop() {
            if (untold) {
                StoryState.Pos at = state.at();
                Op op = library.chapter(state.chapter()).file(at.file()).scene(at.scene()).orElseThrow()
                        .ops().get(at.index());
                unsummarized.add(at.file() + " 第 " + op.line() + " 行");
            }
            untold = false;
        }

        @Override
        public void line(Op.Line line, boolean read, Runnable next) {
            untold = true;
            next.run();
        }

        @Override
        public void menu(Op.Menu menu, IntConsumer pick) {
            stop();
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
            if (cmd.name().equals("簡介")) {
                untold = false;
            } else if (cmd.name().equals("程式")) {
                stop();
            }
            next.run();
        }

        @Override
        public void chapterEnd(String note) {
            stop();
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
                    Explorer stage = new Explorer(picks, state);
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
                    String pov = ch.pov(route).name();
                    if (NOTES.containsKey(pov)) {
                        assertEquals(NOTES.get(pov), stage.chapterEnd, path);
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

    @Test
    void everyStopHasASummaryAfterTheLastLine() {
        Set<String> problems = new LinkedHashSet<>();
        for (Chapter ch : library.chapters()) {
            for (Route route : Route.values()) {
                Deque<List<Integer>> queue = new ArrayDeque<>();
                queue.add(List.of());
                while (!queue.isEmpty()) {
                    List<Integer> picks = queue.removeFirst();
                    StoryState state = new StoryState();
                    state.begin(ch, route, false);
                    Explorer stage = new Explorer(picks, state);
                    new StoryRunner(ch, route, state, new HashSet<>(), stage).start();
                    for (int i = 0; i < stage.waitingOptions; i++) {
                        List<Integer> longer = new ArrayList<>(picks);
                        longer.add(i);
                        queue.addLast(longer);
                    }
                    for (String at : stage.unsummarized) {
                        problems.add(at + "：最後一句台詞和這裡之間要有 @簡介，按「跳過」停在這裡時才有劇情簡介");
                    }
                }
            }
        }
        assertNoProblems(new ArrayList<>(problems));
    }
}
