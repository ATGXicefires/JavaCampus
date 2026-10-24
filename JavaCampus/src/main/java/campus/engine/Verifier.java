package campus.engine;

import campus.content.Check;
import campus.content.SetupVar;
import campus.content.Step;
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

    /**
     * @param key 快取參考解答結果用，同一個步驟要給同一個值
     * @throws IllegalStateException 參考解答本身跑不過，代表案件內容有誤
     */
    public Verdict verify(String key, Step.Form form, List<SetupVar> vars, Check check, String solution, String source) {
        List<Map<String, String>> runs = new ArrayList<>();
        runs.add(Map.of());
        runs.addAll(check.variants());
        List<String> probes = check.type() == Check.Mode.VALUES ? check.exprs() : List.of();
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
            String prefix = overrides.isEmpty() ? "" : "換一組資料（" + describe(overrides) + "）就不對了。";
            if (actual.outcome() != Outcome.OK) {
                return Verdict.fail(prefix + brokenRun(actual.outcome()), actual, overrides);
            }
            Optional<String> diff = check.type() == Check.Mode.OUTPUT
                    ? compareOutput(expected.output(), actual.output())
                    : compareValues(expected.probes(), actual.probes());
            if (diff.isPresent()) {
                return Verdict.fail(prefix + diff.get(), actual, overrides);
            }
        }
        return Verdict.pass(first);
    }

    /** 完整程式不吃案件變數；比對輸出時才需要從 main 跑起，比對值時只要類別定義好就能呼叫。 */
    private RunResult run(Step.Form form, List<String> preamble, String source, Check check, List<String> probes) {
        return form == Step.Form.PROGRAM
                ? sandbox.runProgram(source, check.type() == Check.Mode.OUTPUT, probes)
                : sandbox.run(preamble, source, probes);
    }

    public static List<String> preamble(List<SetupVar> vars, Map<String, String> overrides) {
        return vars.stream().map(v -> v.declaration(overrides.get(v.name()))).toList();
    }

    private static String describe(Map<String, String> overrides) {
        return overrides.entrySet().stream().map(e -> e.getKey() + " = " + e.getValue()).collect(Collectors.joining("，"));
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
            int d = Edits.distance(w, g);
            if (d <= 2 && w.length() > 2) {
                return Optional.of("第 " + (i + 1) + " 行差了 " + d + " 個字：應該是「" + w + "」，你印的是「" + g + "」。");
            }
            return Optional.of("第 " + (i + 1) + " 行不對：你印的是「" + g + "」。");
        }
        return Optional.empty();
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
            if (!want.value().equals(got.value())) {
                return Optional.of("`" + expr + "` 的值不對：你的是 " + got.value() + "。");
            }
        }
        return Optional.empty();
    }
}
