package campus.story;

import campus.story.Op.Kind;
import campus.story.ScriptFile.Scene;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Map.entry;

/** 讀 {@code story/scripts/README.md} 說明的劇本格式。 */
public final class ScriptParser {

    /**
     * 指令參數怎麼切。{@code raw} 的指令把整串文字當成一個參數，因為曲名、筆記本文字裡本來就有空白。
     */
    private record Spec(boolean raw, int min, int max) {}

    private static final Map<String, Spec> COMMANDS = Map.ofEntries(
            entry("背景", new Spec(false, 1, 2)),
            entry("登場", new Spec(false, 2, 3)),
            entry("表情", new Spec(false, 2, 2)),
            entry("退場", new Spec(false, 1, 1)),
            entry("音樂", new Spec(true, 1, 1)),
            entry("環境", new Spec(false, 1, 4)),
            entry("音效", new Spec(true, 1, 1)),
            entry("特效", new Spec(false, 1, 2)),
            entry("暗場", new Spec(false, 0, 0)),
            entry("等待", new Spec(false, 1, 1)),
            entry("手機", new Spec(true, 1, 1)),
            entry("附件", new Spec(true, 2, 2)),
            entry("電話", new Spec(true, 1, 1)),
            entry("文件", new Spec(true, 1, 1)),
            entry("程式", new Spec(true, 1, 1)),
            entry("旗標", new Spec(true, 1, 1)),
            entry("數值", new Spec(false, 1, 2)),
            entry("章末", new Spec(true, 1, 1)));

    public static final List<String> POSITIONS = List.of("左", "中", "右");

    private static final Pattern SCENE = Pattern.compile("\\[(.+)]");
    private static final Pattern COMMAND = Pattern.compile("@(\\S+)\\s*(.*)");
    private static final Pattern OPTION = Pattern.compile("([A-D])[.．、]\\s*(.+)");
    private static final Pattern OS = Pattern.compile("([^\\s：:（）「」\\[\\]]{1,12})（OS）[：:]\\s*(.+)");
    private static final Pattern SAY = Pattern.compile("([^\\s：:（）「」\\[\\]@#>]{1,12})[：:]\\s*(.+)");
    private static final Pattern NARRATION_MARK = Pattern.compile("-{3,}(\\s*旁白\\s*-{3,})?");
    private static final Pattern TARGET = Pattern.compile("→\\s*\\[(.+?)]");
    private static final Pattern EFFECT = Pattern.compile("(AP|TP)\\s*([+-]\\d+)");
    private static final Pattern BRACKET = Pattern.compile("\\[(.+)]");

    private record Raw(int no, String text, boolean indented) {}

    private static final class OptionDraft {
        final int line;
        String text;
        int ap;
        int tp;
        boolean key;
        String target;
        final List<Raw> body = new ArrayList<>();

        OptionDraft(int line) {
            this.line = line;
        }
    }

    private final String file;

    private ScriptParser(String file) {
        this.file = file;
    }

    /** @throws ScriptException 劇本有寫錯的地方 */
    public static ScriptFile parse(String fileName, String text) {
        return new ScriptParser(fileName).parseFile(text);
    }

    private ScriptFile parseFile(String text) {
        List<String> names = new ArrayList<>();
        List<List<Raw>> bodies = new ArrayList<>();
        List<Raw> current = null;
        String[] lines = text.replace("\r\n", "\n").split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].stripTrailing();
            String content = line.strip();
            if (content.isEmpty() || content.startsWith("#") || content.startsWith(">")
                    || NARRATION_MARK.matcher(content).matches()) {
                continue;
            }
            boolean indented = line.startsWith("\t") || line.startsWith("  ");
            Matcher scene = SCENE.matcher(content);
            if (!indented && scene.matches()) {
                String name = scene.group(1).strip();
                if (names.contains(name)) {
                    throw error(i + 1, "場景 [" + name + "] 重複了");
                }
                names.add(name);
                current = new ArrayList<>();
                bodies.add(current);
                continue;
            }
            if (current == null) {
                names.add("");
                current = new ArrayList<>();
                bodies.add(current);
            }
            current.add(new Raw(i + 1, content, indented));
        }
        List<Scene> scenes = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            scenes.add(new Scene(names.get(i), List.copyOf(parseScene(bodies.get(i)))));
        }
        return new ScriptFile(file, List.copyOf(scenes));
    }

    private List<Op> parseScene(List<Raw> lines) {
        List<Op> ops = new ArrayList<>();
        Deque<Integer> open = new ArrayDeque<>();
        int i = 0;
        while (i < lines.size()) {
            Raw r = lines.get(i);
            if (r.text().equals("【選項】")) {
                i = parseMenu(lines, i, ops);
            } else {
                parseSimple(r, ops, open);
                i++;
            }
        }
        closeCheck(open, ops);
        return ops;
    }

    private int parseMenu(List<Raw> lines, int at, List<Op> ops) {
        int menuLine = lines.get(at).no();
        int menuIndex = ops.size();
        ops.add(null);
        List<OptionDraft> drafts = new ArrayList<>();
        int i = at + 1;
        while (i < lines.size()) {
            Raw r = lines.get(i);
            Matcher m = OPTION.matcher(r.text());
            if (!r.indented() && m.matches()) {
                drafts.add(option(m.group(2), r.no()));
            } else if (r.indented() && !drafts.isEmpty()) {
                drafts.getLast().body.add(r);
            } else {
                break;
            }
            i++;
        }
        if (drafts.size() < 2) {
            throw error(menuLine, "【選項】後面至少要有 A、B 兩個選項");
        }
        List<Op.Option> options = new ArrayList<>();
        List<Integer> gotos = new ArrayList<>();
        for (OptionDraft d : drafts) {
            int body = -1;
            if (!d.body.isEmpty()) {
                if (d.target != null) {
                    throw error(d.line, "選項有「→ [場景]」就直接跳走了，底下不能再有縮排的內容");
                }
                body = ops.size();
                Deque<Integer> open = new ArrayDeque<>();
                for (Raw r : d.body) {
                    if (r.text().equals("【選項】")) {
                        throw error(r.no(), "選項的內容裡不能再放【選項】");
                    }
                    parseSimple(r, ops, open);
                }
                closeCheck(open, ops);
                gotos.add(ops.size());
                ops.add(null);
            }
            options.add(new Op.Option(d.text, d.ap, d.tp, d.key, d.target, body));
        }
        int end = ops.size();
        for (int g : gotos) {
            ops.set(g, new Op.Goto(menuLine, end));
        }
        ops.set(menuIndex, new Op.Menu(menuLine, List.copyOf(options), end));
        return i;
    }

    private OptionDraft option(String text, int line) {
        OptionDraft d = new OptionDraft(line);
        Matcher t = TARGET.matcher(text);
        if (t.find()) {
            d.target = t.group(1).strip();
            text = text.substring(0, t.start()) + text.substring(t.end());
        }
        Matcher e = EFFECT.matcher(text);
        while (e.find()) {
            int v = Integer.parseInt(e.group(2));
            if (e.group(1).equals("AP")) {
                d.ap += v;
            } else {
                d.tp += v;
            }
        }
        text = EFFECT.matcher(text).replaceAll("");
        if (text.contains("★")) {
            d.key = true;
            text = text.replace("★", "");
        }
        d.text = text.strip();
        if (d.text.isEmpty()) {
            throw error(line, "選項沒有文字");
        }
        return d;
    }

    private void parseSimple(Raw r, List<Op> ops, Deque<Integer> open) {
        String s = r.text();
        int no = r.no();
        Matcher m = COMMAND.matcher(s);
        if (s.startsWith("@") && m.matches()) {
            command(no, m.group(1), m.group(2).strip(), ops, open);
            return;
        }
        m = OS.matcher(s);
        if (m.matches()) {
            ops.add(new Op.Line(no, Kind.OS, m.group(1), null, unquote(m.group(2).strip())));
            return;
        }
        if (s.startsWith("（") && s.endsWith("）")) {
            ops.add(new Op.Line(no, Kind.DIRECTION, null, null, s));
            return;
        }
        m = SAY.matcher(s);
        if (m.matches()) {
            String rest = m.group(2).strip();
            String action = null;
            if (rest.startsWith("（")) {
                int close = rest.indexOf('）');
                if (close > 0) {
                    action = rest.substring(1, close);
                    rest = rest.substring(close + 1).strip();
                }
            }
            ops.add(new Op.Line(no, Kind.SAY, m.group(1), action, unquote(rest)));
            return;
        }
        ops.add(new Op.Line(no, Kind.NARRATION, null, null, s));
    }

    private void command(int no, String name, String rest, List<Op> ops, Deque<Integer> open) {
        switch (name) {
            case "只限" -> {
                Route route = Route.byLabel(rest)
                        .orElseThrow(() -> error(no, "@只限 後面要寫「學長」或「學妹」"));
                open.push(ops.size());
                ops.add(new Op.Only(no, route, -1));
            }
            case "結束" -> {
                if (open.isEmpty()) {
                    throw error(no, "@結束 前面沒有對應的 @只限");
                }
                int at = open.pop();
                Op.Only only = (Op.Only) ops.get(at);
                ops.set(at, new Op.Only(only.line(), only.route(), ops.size()));
            }
            case "共通" -> ops.add(new Op.Call(no, bracket(no, rest)));
            case "跳到" -> ops.add(new Op.Jump(no, bracket(no, rest)));
            case "如果" -> {
                int arrow = rest.indexOf('→');
                if (arrow < 0) {
                    throw error(no, "@如果 要寫成「@如果 條件 → [場景]」");
                }
                List<Condition> conditions = new ArrayList<>();
                for (String c : rest.substring(0, arrow).strip().split("\\s+")) {
                    try {
                        conditions.add(Condition.parse(c));
                    } catch (IllegalArgumentException e) {
                        throw error(no, e.getMessage());
                    }
                }
                ops.add(new Op.If(no, List.copyOf(conditions), bracket(no, rest.substring(arrow + 1))));
            }
            default -> ops.add(cmd(no, name, rest));
        }
    }

    private Op.Cmd cmd(int no, String name, String rest) {
        Spec spec = COMMANDS.get(name);
        if (spec == null) {
            throw error(no, "不認得的指令 @" + name);
        }
        List<String> args;
        if (name.equals("附件")) {
            args = Arrays.stream(rest.split("→")).map(String::strip).filter(a -> !a.isEmpty()).toList();
        } else if (spec.raw()) {
            args = rest.isEmpty() ? List.of() : List.of(rest);
        } else {
            args = rest.isEmpty() ? List.of() : List.of(rest.split("\\s+"));
        }
        if (args.size() < spec.min() || args.size() > spec.max()) {
            throw error(no, "@" + name + " 的寫法不對，請對照 story/scripts/README.md");
        }
        if (name.equals("登場") && !POSITIONS.contains(args.get(1))) {
            throw error(no, "@登場 的位置要是 左、中、右 其中一個");
        }
        if (name.equals("數值")) {
            for (String a : args) {
                if (!EFFECT.matcher(a).matches()) {
                    throw error(no, "@數值 要寫成 AP+3、TP-2 這樣");
                }
            }
        }
        if (name.equals("等待")) {
            try {
                Double.parseDouble(args.getFirst());
            } catch (NumberFormatException e) {
                throw error(no, "@等待 後面要寫秒數");
            }
        }
        return new Op.Cmd(no, name, args);
    }

    private String bracket(int no, String text) {
        Matcher m = BRACKET.matcher(text.strip());
        if (!m.matches()) {
            throw error(no, "場景名稱要用方括號包起來，例如 [抽籤現場]");
        }
        return m.group(1).strip();
    }

    private void closeCheck(Deque<Integer> open, List<Op> ops) {
        if (!open.isEmpty()) {
            throw error(ops.get(open.peek()).line(), "@只限 沒有對應的 @結束");
        }
    }

    /** 台詞外面的「」由名牌代替，只剝掉整句外圍那一對。 */
    private static String unquote(String s) {
        if (s.startsWith("「") && s.endsWith("」") && s.indexOf('「', 1) < 0) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private ScriptException error(int line, String message) {
        return new ScriptException(file, line, message);
    }

    /** 指令效果字串（例如 {@code AP+3}）拆成兩個數值。 */
    static int[] effect(List<String> args) {
        int[] apTp = new int[2];
        for (String a : args) {
            Matcher m = EFFECT.matcher(a);
            if (m.matches()) {
                apTp[m.group(1).equals("AP") ? 0 : 1] += Integer.parseInt(m.group(2));
            }
        }
        return apTp;
    }
}
