package campus.story;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** {@code @如果} 的一個條件，例如 {@code AP>=80}、{@code 旗標:看到雨中的窗戶}、{@code 結局=True}。 */
public record Condition(String subject, String op, String value) {

    private static final Pattern COMPARE = Pattern.compile("(AP|TP|關鍵)(>=|<=|>|<|=)(\\d+)");
    private static final Pattern ENDING = Pattern.compile("結局=(True|Good|Normal|Bad)");
    private static final Pattern FLAG = Pattern.compile("旗標[:：](.+)");

    /** @throws IllegalArgumentException 寫法不認得 */
    public static Condition parse(String text) {
        Matcher m = COMPARE.matcher(text);
        if (m.matches()) {
            return new Condition(m.group(1), m.group(2), m.group(3));
        }
        m = ENDING.matcher(text);
        if (m.matches()) {
            return new Condition("結局", "=", m.group(1));
        }
        m = FLAG.matcher(text);
        if (m.matches()) {
            return new Condition("旗標", "=", m.group(1).strip());
        }
        throw new IllegalArgumentException("看不懂的條件「" + text + "」");
    }

    public boolean test(StoryState s) {
        return switch (subject) {
            case "AP" -> compare(s.ap());
            case "TP" -> compare(s.tp());
            case "關鍵" -> compare(s.keyCount());
            case "結局" -> Endings.of(s.ap(), s.tp(), s.keyCount()).code.equals(value);
            default -> s.hasFlag(value);
        };
    }

    private boolean compare(int actual) {
        int v = Integer.parseInt(value);
        return switch (op) {
            case ">=" -> actual >= v;
            case "<=" -> actual <= v;
            case ">" -> actual > v;
            case "<" -> actual < v;
            default -> actual == v;
        };
    }
}
