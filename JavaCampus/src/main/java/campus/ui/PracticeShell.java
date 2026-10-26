package campus.ui;

import campus.lesson.Lesson;
import campus.progress.Progress;
import campus.story.Route;
import campus.story.StoryLibrary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 練習模式的終端機：目前在哪個資料夾、指令、Tab 補完和打過的指令。只處理文字，不碰 JavaFX，畫面在 {@link PracticeView}。
 * 根目錄 {@code ~/lessons} 底下每章一個資料夾 {@code chNN}，裡面的題目用 id 去掉章號的部分當名字，例如 {@code k1}、{@code s2}。
 */
final class PracticeShell {

    enum Style { USER, PATH, PLAIN, DIR, ID, DONE, GROUP, ERROR }

    record Span(String text, Style style) {}

    /** 畫面上的一行；{@code link} 是點了要去的章（{@code ch02}）或題目（{@code ch02-k1}），不能點的是 null。 */
    record Line(List<Span> spans, String link) {

        String text() {
            StringBuilder b = new StringBuilder();
            spans.forEach(s -> b.append(s.text()));
            return b.toString();
        }
    }

    /** 打完一行指令之後，畫面除了印出 {@link Result#lines} 還要做的事。 */
    enum Action { NONE, CLEAR, RUN, EXIT }

    /** @param lesson 要開的題目，只有 {@link Action#RUN} 才有 */
    record Result(List<Line> lines, Action action, Lesson lesson) {}

    /** 按 Tab 的結果：輸入列換成 {@code input}；有好幾個可能又補不下去時，{@code options} 是要列給玩家看的名字。 */
    record Completion(String input, List<String> options) {}

    /** 結局小題要在劇情裡達成那個結局才寫得到；劇情裡沒寫過就當作不存在，清單才不會先暴雷。 */
    private static final Set<String> ENDINGS = Set.of("ch11-kt", "ch11-sg", "ch12-kt", "ch12-sb");
    private static final List<String> COMMANDS = List.of("ls", "cd", "run", "help", "clear", "exit");
    private static final Pattern LESSON_ID = Pattern.compile("ch(\\d+)-(.+)");
    private static final String HOME = "~/lessons";
    private static final int ROOT = 0;

    private final List<StoryLibrary.Listing> chapters;
    private final List<Lesson> lessons;
    private final Progress.LessonRecord story;
    private final Progress.LessonRecord practice;
    private final List<String> history = new ArrayList<>();
    /** 用 ↑↓ 翻到第幾個打過的指令；等於 {@code history.size()} 代表沒在翻。 */
    private int browsing;
    /** 開始往回翻之前還沒送出的輸入，翻回最後時還給玩家。 */
    private String draft = "";
    /** 目前所在的章號；{@link #ROOT} 是 {@code ~/lessons}。 */
    private int cwd = ROOT;

    /**
     * @param story    劇情的紀錄，決定結局小題看不看得到
     * @param practice 練習的紀錄，寫完的題目在清單上打勾
     */
    PracticeShell(List<StoryLibrary.Listing> chapters, Collection<Lesson> lessons, Progress.LessonRecord story,
                  Progress.LessonRecord practice) {
        this.chapters = chapters;
        this.lessons = lessons.stream().sorted(Comparator.comparing(Lesson::id)).toList();
        this.story = story;
        this.practice = practice;
    }

    List<Span> prompt() {
        return List.of(new Span("eh@xiaobai", Style.USER), new Span(":", Style.PLAIN), new Span(path(), Style.PATH),
                new Span("$ ", Style.PLAIN));
    }

    /** 視窗標題列跟著目前的資料夾走，跟一般的終端機一樣。 */
    String title() {
        return "eh@xiaobai: " + path();
    }

    private String path() {
        return cwd == ROOT ? HOME : HOME + "/" + dirName(cwd);
    }

    Result execute(String input) {
        List<Line> out = new ArrayList<>();
        out.add(echo(input));
        String command = input.strip();
        remember(command);
        if (command.isEmpty()) {
            return new Result(out, Action.NONE, null);
        }
        String[] words = command.toLowerCase(Locale.ROOT).split("\\s+");
        String arg = words.length > 1 ? words[1] : null;
        switch (words[0]) {
            case "ls" -> {
                int dir = arg == null ? cwd : resolve(arg);
                if (dir < 0) {
                    out.add(error("ls: " + arg + ": 沒有這個資料夾"));
                } else {
                    out.addAll(dir == ROOT ? listChapters() : listLessons(dir));
                }
            }
            case "cd" -> {
                int dir = arg == null ? ROOT : resolve(arg);
                if (dir < 0) {
                    out.add(error("cd: " + arg + ": 沒有這個資料夾"));
                } else {
                    cwd = dir;
                }
            }
            case "run" -> {
                if (arg == null) {
                    out.add(error("run: 要寫題目的名字，例如 run " + (cwd == ROOT ? "ch01-k1" : "k1")));
                    break;
                }
                Lesson l = find(arg);
                if (l != null) {
                    return new Result(out, Action.RUN, l);
                }
                out.add(error(cwd == ROOT && !LESSON_ID.matcher(arg).matches()
                        ? "run: " + arg + ": 這裡沒有這一題，先 cd 到那一章"
                        : "run: " + arg + ": 沒有這一題"));
            }
            case "help" -> out.addAll(help());
            case "clear" -> {
                return new Result(List.of(), Action.CLEAR, null);
            }
            case "exit" -> {
                return new Result(out, Action.EXIT, null);
            }
            default -> out.add(error(words[0] + ": 找不到指令，打 help 看看有哪些"));
        }
        return new Result(out, Action.NONE, null);
    }

    /**
     * 點了清單上的章或題目：換成對應的指令來執行，跟打字一樣會印出來、記進打過的指令。
     * 點章的時候 cd 進去再 ls，只用滑鼠也看得到裡面的題目。
     */
    Result click(String link) {
        Lesson l = lessons.stream().filter(x -> x.id().equals(link)).findFirst().orElse(null);
        if (l == null) {
            List<Line> lines = new ArrayList<>(execute("cd " + link).lines());
            lines.addAll(execute("ls").lines());
            return new Result(lines, Action.NONE, null);
        }
        // 清單可能是在別的資料夾列出來的，不在那一章就要打完整的名字
        return execute("run " + (chapterOf(l) == cwd ? shortName(l) : l.id()));
    }

    /** 從題目回到終端機：寫完了就補一行打勾，像程式結束時印出結果；還沒寫完就什麼都不印。 */
    List<Line> back(Lesson lesson) {
        if (!done(practice, lesson)) {
            return List.of();
        }
        return List.of(new Line(List.of(new Span("✓ ", Style.DONE), new Span(lesson.id(), Style.ID),
                new Span(" " + lesson.title(), Style.PLAIN)), lesson.id()));
    }

    Completion complete(String input) {
        int cut = input.lastIndexOf(' ') + 1;
        String head = input.substring(0, cut);
        String word = input.substring(cut).toLowerCase(Locale.ROOT);
        Stream<String> names = head.isBlank() ? COMMANDS.stream()
                : namesFor(head.strip().split("\\s+")[0].toLowerCase(Locale.ROOT), word);
        List<String> found = names.filter(n -> n.startsWith(word)).toList();
        if (found.isEmpty()) {
            return new Completion(input, List.of());
        }
        if (found.size() == 1) {
            return new Completion(head + found.getFirst() + " ", List.of());
        }
        String common = commonPrefix(found);
        if (common.length() > word.length()) {
            return new Completion(head + common, List.of());
        }
        return new Completion(input, found);
    }

    /** 跟 bash 一樣，列候選之前先把目前的輸入印成一行。 */
    List<Line> optionLines(String input, List<String> options) {
        return List.of(echo(input), new Line(List.of(new Span(String.join("  ", options), Style.PLAIN)), null));
    }

    /** ↑：往前翻一個打過的指令。 */
    String older(String current) {
        if (browsing == history.size()) {
            draft = current;
        }
        if (browsing > 0) {
            browsing--;
        }
        return browsing < history.size() ? history.get(browsing) : current;
    }

    /** ↓：往後翻；翻過最後一個就回到還沒送出的輸入。沒在翻的時候不動。 */
    String newer(String current) {
        if (browsing == history.size()) {
            return current;
        }
        browsing++;
        return browsing < history.size() ? history.get(browsing) : draft;
    }

    private void remember(String command) {
        if (!command.isEmpty() && (history.isEmpty() || !history.getLast().equals(command))) {
            history.add(command);
        }
        browsing = history.size();
        draft = "";
    }

    private Line echo(String input) {
        List<Span> spans = new ArrayList<>(prompt());
        spans.add(new Span(input, Style.PLAIN));
        return new Line(spans, null);
    }

    private static Line error(String message) {
        return new Line(List.of(new Span(message, Style.ERROR)), null);
    }

    private List<Line> listChapters() {
        return chapters.stream().map(c -> new Line(List.of(new Span("  ", Style.PLAIN),
                new Span(dirName(c.number()), Style.DIR), new Span("  " + c.title(), Style.PLAIN)),
                dirName(c.number()))).toList();
    }

    /** 學妹、學長各一組；練習裡寫完的題目在後面打勾，勾勾對齊同一欄。 */
    private List<Line> listLessons(int chapter) {
        List<Lesson> here = visible().filter(l -> chapterOf(l) == chapter).toList();
        int width = here.stream().mapToInt(l -> columns(l.title())).max().orElse(0);
        List<Line> out = new ArrayList<>();
        for (Route r : List.of(Route.KOUHAI, Route.SENPAI)) {
            List<Lesson> group = here.stream().filter(l -> l.route().equals(r.label))
                    .sorted(Comparator.comparing(PracticeShell::shortName)).toList();
            if (group.isEmpty()) {
                continue;
            }
            out.add(new Line(List.of(new Span("  ", Style.PLAIN), new Span(r.label, Style.GROUP)), null));
            for (Lesson l : group) {
                List<Span> spans = new ArrayList<>(List.of(new Span("    ", Style.PLAIN),
                        new Span(shortName(l), Style.ID), new Span("  " + l.title(), Style.PLAIN)));
                if (done(practice, l)) {
                    spans.add(new Span(" ".repeat(width - columns(l.title()) + 2), Style.PLAIN));
                    spans.add(new Span("✓", Style.DONE));
                }
                out.add(new Line(spans, l.id()));
            }
        }
        return out;
    }

    private static List<Line> help() {
        String[][] rows = {
                {"ls", "列出章節或題目"},
                {"cd ch02", "進到那一章，cd .. 回上一層"},
                {"run k1", "寫那一題；不在那一章就打 run ch02-k1"},
                {"clear", "清除畫面"},
                {"exit", "回到標題"}};
        List<Line> out = new ArrayList<>();
        for (String[] row : rows) {
            out.add(new Line(List.of(new Span("  ", Style.PLAIN), new Span(row[0], Style.ID),
                    new Span(" ".repeat(10 - row[0].length()) + row[1], Style.PLAIN)), null));
        }
        out.add(new Line(List.of(new Span("  Tab 補完名稱，↑↓ 叫出打過的指令", Style.PLAIN)), null));
        return out;
    }

    /** 在章裡面補這一章的題號；開頭打了 c 或在根目錄，才補完整的名字。 */
    private Stream<String> namesFor(String command, String word) {
        return switch (command) {
            case "cd", "ls" -> chapters.stream().map(c -> dirName(c.number()));
            case "run" -> cwd != ROOT && !word.startsWith("c")
                    ? visible().filter(l -> chapterOf(l) == cwd).map(PracticeShell::shortName).sorted()
                    : visible().map(Lesson::id);
            default -> Stream.empty();
        };
    }

    /** 路徑只看最後一段：{@code ~}、{@code ..} 回到 {@code ~/lessons}，{@code chNN} 是那一章，在哪一章底下打都一樣。找不到是 -1。 */
    private int resolve(String path) {
        String p = path.length() > 1 && path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        String last = p.substring(p.lastIndexOf('/') + 1);
        return switch (last) {
            case "~", "..", "lessons" -> ROOT;
            case "." -> cwd;
            default -> chapters.stream().filter(c -> dirName(c.number()).equals(last)).mapToInt(StoryLibrary.Listing::number)
                    .findFirst().orElse(-1);
        };
    }

    /** 完整的 {@code ch02-k1} 在哪裡都能用；只寫 {@code k1} 就是目前這一章的。 */
    private Lesson find(String name) {
        String id = LESSON_ID.matcher(name).matches() ? name : cwd == ROOT ? null : dirName(cwd) + "-" + name;
        return visible().filter(l -> l.id().equals(id)).findFirst().orElse(null);
    }

    private Stream<Lesson> visible() {
        return lessons.stream().filter(l -> !ENDINGS.contains(l.id()) || done(story, l));
    }

    private static boolean done(Progress.LessonRecord record, Lesson l) {
        return l.steps().stream().allMatch(s -> record.isSolved(l.id(), s.id()));
    }

    private static String dirName(int chapter) {
        return String.format(Locale.ROOT, "ch%02d", chapter);
    }

    /** id 不是 {@code chNN-} 開頭的題目不屬於任何一章，是 -1。 */
    private static int chapterOf(Lesson l) {
        Matcher m = LESSON_ID.matcher(l.id());
        return m.matches() ? Integer.parseInt(m.group(1)) : -1;
    }

    private static String shortName(Lesson l) {
        return l.id().substring(l.id().indexOf('-') + 1);
    }

    private static String commonPrefix(List<String> names) {
        String common = names.getFirst();
        for (String n : names) {
            while (!n.startsWith(common)) {
                common = common.substring(0, common.length() - 1);
            }
        }
        return common;
    }

    /** 等寬字型裡中文字佔兩格；勾勾要對齊同一欄，得照格數補空白。 */
    private static int columns(String s) {
        return s.codePoints().map(c -> wide(c) ? 2 : 1).sum();
    }

    private static boolean wide(int c) {
        return c >= 0x1100 && c <= 0x115F || c >= 0x2E80 && c <= 0xA4CF || c >= 0xAC00 && c <= 0xD7A3
                || c >= 0xF900 && c <= 0xFAFF || c >= 0xFE30 && c <= 0xFE4F || c >= 0xFF00 && c <= 0xFF60
                || c >= 0xFFE0 && c <= 0xFFE6 || c >= 0x20000;
    }
}
