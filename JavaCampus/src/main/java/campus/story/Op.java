package campus.story;

import java.util.List;

/**
 * 劇本解析後的一行指令。選項內容和 {@code @只限} 區塊在解析時就攤平成同一個場景裡的連續指令，
 * 靠 {@link Goto} 和 {@link Only} 跳過，所以播放位置只要「場景名稱加索引」就能存檔。
 */
public sealed interface Op {

    /** 原始檔案的行號，從 1 起算。 */
    int line();

    enum Kind { SAY, OS, NARRATION, DIRECTION }

    /** 台詞、內心 OS、旁白或舞台指示。旁白和舞台指示的 {@code who} 是 null。 */
    record Line(int line, Kind kind, String who, String action, String text) implements Op {}

    /**
     * @param target 選了直接跳到的場景；沒有就是 null
     * @param body   選項內容在場景裡的起始索引；沒有內容就是 -1
     */
    record Option(String text, int ap, int tp, boolean key, String target, int body) {}

    /** @param end 選項區塊之後第一行的索引，沒有內容也沒有跳轉的選項選完就到這裡 */
    record Menu(int line, List<Option> options, int end) implements Op {}

    record Goto(int line, int index) implements Op {}

    /** 視角不是 {@code route} 時直接跳到 {@code end}。 */
    record Only(int line, Route route, int end) implements Op {}

    /** {@code @共通}：播一段共通場景，播完回來。 */
    record Call(int line, String scene) implements Op {}

    /** {@code @跳到}：同一個檔案裡換場景，不回來。 */
    record Jump(int line, String scene) implements Op {}

    record If(int line, List<Condition> conditions, String scene) implements Op {}

    /** 其他 {@code @} 指令，交給播放器或畫面處理。 */
    record Cmd(int line, String name, List<String> args) implements Op {

        public String arg(int i) {
            return i < args.size() ? args.get(i) : null;
        }
    }
}
