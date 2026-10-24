package campus.lesson;

/**
 * 題目預先放進執行環境的變數。{@code value} 是 Java 字面值原文，例如 {@code "\"林雅婷\""} 或 {@code "20"}。
 */
public record SetupVar(String name, String type, String value) {

    public String declaration(String overrideValue) {
        return type + " " + name + " = " + (overrideValue != null ? overrideValue : value) + ";";
    }
}
