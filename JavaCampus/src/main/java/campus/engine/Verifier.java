package campus.engine;

import campus.lesson.Check;
import campus.lesson.SetupVar;
import campus.lesson.SourceFile;
import campus.lesson.Step;
import campus.engine.RunResult.Outcome;
import campus.engine.RunResult.Probe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** 跑參考解答與玩家程式，比對兩者的結果。原始資料與每一組替換資料都要對。 */
public final class Verifier {

    private final Sandbox sandbox;
    private final Map<String, RunResult> expectedCache = new ConcurrentHashMap<>();

    public Verifier(Sandbox sandbox) {
        this.sandbox = sandbox;
    }

    /** 只有一個檔案時的簡便寫法。 */
    public Verdict verify(String key, Step.Form form, List<SetupVar> vars, Check check, String solution, String source) {
        return verify(key, form, vars, check, List.of(new SourceFile(form.fileName, solution)),
                List.of(new SourceFile(form.fileName, source)));
    }

    /**
     * @param key      快取參考解答結果用，同一個步驟要給同一個值
     * @param solution 參考解答的檔案；片段題只有一個
     * @param source   玩家的檔案，完整程式會全部一起編譯
     * @throws IllegalStateException 參考解答本身跑不過，代表案件內容有誤
     */
    public Verdict verify(String key, Step.Form form, List<SetupVar> vars, Check check, List<SourceFile> solution,
                          List<SourceFile> source) {
        List<Map<String, String>> runs = new ArrayList<>();
        runs.add(Map.of());
        runs.addAll(check.variants());
        List<String> probes = check.exprs();
        RunResult first = null;
        for (int i = 0; i < runs.size(); i++) {
            Map<String, String> overrides = runs.get(i);
            List<String> preamble = preamble(vars, overrides);
            RunResult expected = expectedCache.computeIfAbsent(key + "#" + i,
                    k -> run(form, preamble, solution, check, probes));
            if (expected.outcome() != Outcome.OK || expected.probes().stream().anyMatch(p -> p.error() != null)) {
                throw new IllegalStateException("參考解答跑不過：" + key + " " + expected);
            }
            RunResult actual = run(form, preamble, source, check, probes);
            if (first == null) {
                first = actual;
            }
            Optional<String> problem = overrides.isEmpty()
                    ? problem(check, expected, actual)
                    : variantProblem(overrides, check, expected, actual, first);
            if (problem.isPresent()) {
                return Verdict.fail(problem.get(), actual, overrides);
            }
        }
        return Verdict.pass(first);
    }

    private static Optional<String> problem(Check check, RunResult expected, RunResult actual) {
        if (actual.outcome() != Outcome.OK) {
            return Optional.of(brokenRun(actual.outcome()));
        }
        Optional<String> output = check.type() == Check.Mode.OUTPUT
                ? compareOutput(expected.output(), actual.output()) : Optional.empty();
        return output.or(() -> compareValues(expected.probes(), actual.probes()));
    }

    /**
     * 原始資料對了，換一組才錯。先說換了什麼，玩家才知道這些結果是怎麼來的；結果跟原始資料一模一樣，就是沒有用到換掉的變數。
     * 只差一兩個字的話，錯的是那一行字，先講差在哪裡。
     *
     * @param first 同一份程式用原始資料跑的結果，已經確定是對的
     */
    private static Optional<String> variantProblem(Map<String, String> overrides, Check check, RunResult expected,
                                                   RunResult actual, RunResult first) {
        String change = overrides.entrySet().stream().map(e -> "`" + e.getKey() + "` 改成 " + e.getValue())
                .collect(Collectors.joining("、"));
        String follow = "，沒有跟著 " + overrides.keySet().stream().map(n -> "`" + n + "`")
                .collect(Collectors.joining("、")) + " 變";
        if (actual.outcome() != Outcome.OK) {
            return Optional.of(change + " 再跑一次，" + brokenRun(actual.outcome()));
        }
        if (check.type() == Check.Mode.OUTPUT) {
            List<String> want = lines(expected.output());
            List<String> got = lines(actual.output());
            boolean same = got.equals(lines(first.output()));
            if (got.isEmpty() && !want.isEmpty()) {
                return Optional.of(change + " 再跑一次，你的程式沒有印出任何東西。");
            }
            if (want.size() != got.size()) {
                return Optional.of(change + " 再跑一次，應該印 " + want.size() + " 行，你的程式"
                        + (same ? "還是印 " + got.size() + " 行" + follow : "印了 " + got.size() + " 行") + "。");
            }
            for (int i = 0; i < want.size(); i++) {
                String w = want.get(i);
                String g = got.get(i);
                if (w.equals(g)) {
                    continue;
                }
                if (!same && isTypo(w, g)) {
                    return Optional.of(typo(i, w, g) + "這一行是 " + change + " 再跑一次時印出來的。");
                }
                return Optional.of(change + " 再跑一次，第 " + (i + 1) + " 行應該是「" + visible(w) + "」，你印的"
                        + (same ? "還是「" + visible(g) + "」" + follow : "是「" + visible(g) + "」") + "。");
            }
        }
        for (int i = 0; i < expected.probes().size(); i++) {
            Probe want = expected.probes().get(i);
            Probe got = actual.probes().get(i);
            if (got.error() != null || !want.type().equals(got.type())) {
                return compareValues(List.of(want), List.of(got)).map(m -> change + " 再算一次，" + m);
            }
            if (isVoid(want)) {
                Optional<String> printed = printedProblem(want, got);
                if (printed.isPresent()) {
                    return printed.map(m -> change + " 再算一次，" + m);
                }
            } else if (!want.value().equals(got.value())) {
                String mine = got.value().equals(first.probes().get(i).value())
                        ? "還是 " + got.value() + follow : "是 " + got.value();
                return Optional.of(change + " 再算一次，`" + want.expr() + "` 應該是 " + want.value() + "，你的" + mine + "。");
            }
        }
        return Optional.empty();
    }

    /** 完整程式不吃案件變數；比對輸出時才需要從 main 跑起，比對值時只要類別定義好就能呼叫。 */
    private RunResult run(Step.Form form, List<String> preamble, List<SourceFile> files, Check check, List<String> probes) {
        return form == Step.Form.PROGRAM
                ? sandbox.runProgram(files, check.type() == Check.Mode.OUTPUT, probes)
                : sandbox.run(preamble, files.getFirst().text(), probes);
    }

    public static List<String> preamble(List<SetupVar> vars, Map<String, String> overrides) {
        return vars.stream().map(v -> v.declaration(overrides.get(v.name()))).toList();
    }

    private static String brokenRun(Outcome outcome) {
        return switch (outcome) {
            case COMPILE_ERROR -> "程式有錯誤，還沒辦法執行。先照下方的說明修正。";
            case RUNTIME_ERROR -> "程式執行到一半出錯了。";
            case TIMEOUT -> "程式跑太久，被強制停止了。";
            case STOPPED -> "程式被停止了。";
            case EXITED -> "程式呼叫了 `System.exit()`，沒有跑完。";
            case OK -> throw new IllegalArgumentException();
        };
    }

    static Optional<String> compareOutput(String expectedText, String actualText) {
        List<String> want = lines(expectedText);
        List<String> got = lines(actualText);
        if (got.isEmpty() && !want.isEmpty()) {
            return Optional.of("你的程式沒有印出任何東西。用 `System.out.println(…);` 把結果印出來。");
        }
        if (want.size() != got.size()) {
            return Optional.of("預期輸出 " + want.size() + " 行，你的程式印了 " + got.size() + " 行。");
        }
        for (int i = 0; i < want.size(); i++) {
            String w = want.get(i);
            String g = got.get(i);
            if (w.equals(g)) {
                continue;
            }
            if (isTypo(w, g)) {
                return Optional.of(typo(i, w, g));
            }
            return Optional.of("第 " + (i + 1) + " 行不對：你印的是「" + visible(g) + "」。");
        }
        return Optional.empty();
    }

    private static boolean isTypo(String want, String got) {
        return want.length() > 2 && Edits.distance(want, got) <= 2;
    }

    /** 只差一兩個字：直接說差在哪裡，空格看不出來，所以也講出來。 */
    private static String typo(int line, String want, String got) {
        List<String> what = Edits.changes(want, got).stream().map(c -> describe(c, want)).toList();
        String where = what.size() == 1
                ? "第 " + (line + 1) + " 行" + what.getFirst() + "："
                : "第 " + (line + 1) + " 行有 " + what.size() + " 個地方不一樣：" + String.join("，", what) + "。";
        return where + "應該是「" + visible(want) + "」，你印的是「" + visible(got) + "」。";
    }

    /** 插入和刪除用前一個字指出位置，比數第幾個字直覺。 */
    private static String describe(Edits.Change c, String want) {
        return switch (c.kind()) {
            case INSERT -> (c.at() == 0 ? "開頭" : c.at() == want.length() ? "最後" : "在" + name(want.charAt(c.at() - 1)) + "後面")
                    + "多了" + one(c.to());
            case DELETE -> (c.at() == 0 ? "開頭" : c.at() == want.length() - 1 ? "最後" : "在" + name(want.charAt(c.at() - 1)) + "後面")
                    + "少了" + one(c.from());
            case REPLACE -> c.to() - c.from() == 0xFEE0 ? "把半形的" + name(c.from()) + "打成了全形的" + name(c.to())
                    : c.from() - c.to() == 0xFEE0 ? "把全形的" + name(c.from()) + "打成了半形的" + name(c.to())
                    : "把" + name(c.from()) + "打成了" + name(c.to());
        };
    }

    private static String one(char c) {
        return c == ' ' ? "一個空格" : name(c);
    }

    private static String name(char c) {
        return c == ' ' ? "空格" : c == '　' ? "全形空格" : "「" + c + "」";
    }

    /** 引號裡的輸出把空格畫成 ␣，多一個、少一個才看得出來。 */
    private static String visible(String line) {
        return line.replace(' ', '␣');
    }

    /** 去掉每行尾端看不見的空白與最後的空行，那些差異不算錯。 */
    private static List<String> lines(String text) {
        List<String> out = new ArrayList<>(Arrays.stream(text.replace("\r\n", "\n").split("\n", -1))
                .map(String::stripTrailing).toList());
        while (!out.isEmpty() && out.getLast().isEmpty()) {
            out.removeLast();
        }
        return out;
    }

    static Optional<String> compareValues(List<Probe> expected, List<Probe> actual) {
        for (int i = 0; i < expected.size(); i++) {
            Probe want = expected.get(i);
            Probe got = actual.get(i);
            String expr = want.expr();
            boolean isName = expr.matches("[\\p{L}_$][\\p{L}\\p{N}_$]*");
            if (got.error() != null) {
                return Optional.of(isName
                        ? "找不到 `" + expr + "`：題目要你把答案存在名為 `" + expr + "` 的變數裡。"
                        : "沒辦法執行 `" + expr + "`：" + got.error());
            }
            if (!want.type().equals(got.type())) {
                return Optional.of("`" + expr + "` 的型別應該是 " + want.type() + "，你的是 " + got.type() + "。");
            }
            if (isVoid(want)) {
                Optional<String> printed = printedProblem(want, got);
                if (printed.isPresent()) {
                    return printed;
                }
            } else if (!want.value().equals(got.value())) {
                return Optional.of("`" + expr + "` 的值不對：你的是 " + got.value() + "。");
            }
        }
        return Optional.empty();
    }

    private static boolean isVoid(Probe p) {
        return p.type().equals("void");
    }

    /** 回傳 {@code void} 的呼叫沒有值，比的是它印出來的字。 */
    private static Optional<String> printedProblem(Probe want, Probe got) {
        if (lines(want.output()).equals(lines(got.output()))) {
            return Optional.empty();
        }
        String should = lines(want.output()).isEmpty() ? "不用印出東西" : "應該印出" + quoted(want.output());
        String mine = lines(got.output()).isEmpty() ? "什麼都沒印" : "印出" + quoted(got.output());
        return Optional.of("`" + want.expr() + "` " + should + "，你的" + mine + "。");
    }

    /** 印出來的每一行各用一組引號括起來。 */
    private static String quoted(String text) {
        return lines(text).stream().map(l -> "「" + visible(l) + "」").collect(Collectors.joining());
    }
}
