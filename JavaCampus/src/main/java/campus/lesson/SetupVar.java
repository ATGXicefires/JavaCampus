package campus.lesson;

/**
 * 題目預先放進執行環境的變數。{@code value} 是 Java 字面值原文，例如 {@code "\"林雅婷\""} 或 {@code "20"}。
 *
 * @param note 這個變數裝的是什麼，例如「要罰寫的遍數」；編輯器上方那行宣告會把它寫成行尾註解，可以省略
 */
public record SetupVar(String name, String type, String value, String note) {

    public String declaration(String overrideValue) {
        return type + " " + name + " = " + (overrideValue != null ? overrideValue : value) + ";";
    }
}
