package detective.engine;

import java.util.Map;

/**
 * 提交的判定結果。
 *
 * @param run     決定這個判定的那次玩家執行
 * @param variant 失敗發生在哪一組替換資料；原始資料或通過時為空
 */
public record Verdict(boolean passed, String message, RunResult run, Map<String, String> variant) {

    static Verdict pass(RunResult run) {
        return new Verdict(true, "", run, Map.of());
    }

    static Verdict fail(String message, RunResult run, Map<String, String> variant) {
        return new Verdict(false, message, run, variant);
    }
}
