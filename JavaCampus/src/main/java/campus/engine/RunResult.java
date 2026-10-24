package campus.engine;

import java.util.List;
import java.util.Optional;

/**
 * 一次執行的結果。
 *
 * @param output    標準輸出；編譯錯誤時一律為空，因為真正的 Java 會在編譯階段就停下
 * @param truncated 輸出超過上限而被截斷
 * @param problems  錯誤與警告，位置對應玩家原始碼
 * @param probes    執行完後對每個運算式求值的結果，順序與要求時相同
 */
public record RunResult(Outcome outcome, String output, boolean truncated, List<Problem> problems, List<Probe> probes) {

    public enum Outcome { OK, COMPILE_ERROR, RUNTIME_ERROR, TIMEOUT, STOPPED, EXITED }

    public enum Severity { ERROR, WARNING }

    /** @param raw 編譯器或例外的原始訊息，玩家可以展開查看 */
    public record Problem(Severity severity, int start, int end, String message, String raw) {}

    /** {@code error} 不為 null 時代表這個運算式沒辦法求值，{@code type} 與 {@code value} 為 null。 */
    public record Probe(String expr, String type, String value, String error) {}

    public RunResult {
        problems = List.copyOf(problems);
        probes = List.copyOf(probes);
    }

    public Optional<Problem> firstError() {
        return problems.stream().filter(p -> p.severity() == Severity.ERROR).findFirst();
    }
}
