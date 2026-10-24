package campus.story;

/** 劇本寫錯的地方。訊息會標出檔名和行號，給寫劇本的人直接找到那一行。 */
public final class ScriptException extends RuntimeException {

    public ScriptException(String file, int line, String message) {
        super(file + " 第 " + line + " 行：" + message);
    }
}
