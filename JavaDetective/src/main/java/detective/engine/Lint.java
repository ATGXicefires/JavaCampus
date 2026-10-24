package detective.engine;

import detective.engine.JavaTokens.Kind;
import detective.engine.JavaTokens.Token;
import detective.engine.RunResult.Problem;
import detective.engine.RunResult.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** 能編譯、但幾乎一定不是玩家本意的寫法。只提醒，不擋執行。 */
final class Lint {

    private Lint() {}

    static List<Problem> check(String source, Set<String> stringVariables) {
        List<Token> tokens = JavaTokens.tokenize(source).stream().filter(t -> t.kind() != Kind.COMMENT).toList();
        List<Problem> out = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if ((t.text().equals("==") || t.text().equals("!=")) && i > 0 && i + 1 < tokens.size()) {
                Token left = tokens.get(i - 1);
                Token right = tokens.get(i + 1);
                if (isString(left, stringVariables) && !isNull(right) || isString(right, stringVariables) && !isNull(left)) {
                    out.add(new Problem(Severity.WARNING, left.start(), right.end(),
                            "比較字串的內容要用 `.equals(…)`，例如 `a.equals(\"hi\")`。`==` 比的是「是不是同一個物件」，結果常常出乎意料。",
                            "String compared with " + t.text()));
                }
            }
            if (isControlKeyword(t, i > 0 ? tokens.get(i - 1) : null)) {
                int close = matchingParen(tokens, i + 1);
                if (close > 0 && close + 1 < tokens.size() && tokens.get(close + 1).text().equals(";")) {
                    Token semi = tokens.get(close + 1);
                    out.add(new Problem(Severity.WARNING, semi.start(), semi.end(),
                            "`" + t.text() + " (…)` 的右括號後面多了分號，所以它的條件不會控制下一行的程式。把這個分號刪掉。",
                            "empty statement after " + t.text()));
                }
            }
        }
        return out;
    }

    private static boolean isString(Token t, Set<String> stringVariables) {
        return t.kind() == Kind.STRING || t.kind() == Kind.IDENT && stringVariables.contains(t.text());
    }

    private static boolean isNull(Token t) {
        return t.text().equals("null");
    }

    /** do-while 的 {@code while (…);} 是合法寫法，前一個 token 是 {@code }} 時不算。 */
    private static boolean isControlKeyword(Token t, Token previous) {
        return switch (t.text()) {
            case "if", "for" -> t.kind() == Kind.KEYWORD;
            case "while" -> t.kind() == Kind.KEYWORD && (previous == null || !previous.text().equals("}"));
            default -> false;
        };
    }

    private static int matchingParen(List<Token> tokens, int open) {
        if (open >= tokens.size() || !tokens.get(open).text().equals("(")) {
            return -1;
        }
        int depth = 0;
        for (int i = open; i < tokens.size(); i++) {
            String s = tokens.get(i).text();
            if (s.equals("(")) {
                depth++;
            } else if (s.equals(")") && --depth == 0) {
                return i;
            }
        }
        return -1;
    }
}
