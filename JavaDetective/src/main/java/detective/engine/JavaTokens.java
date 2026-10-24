package detective.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** 寬鬆的 Java 斷詞器，給語法上色與 Lint 共用；遇到不完整的程式碼也不會失敗。 */
public final class JavaTokens {

    public enum Kind { KEYWORD, IDENT, STRING, CHAR, NUMBER, COMMENT, OPERATOR, PUNCT, OTHER }

    public record Token(Kind kind, int start, int end, String text) {}

    public static final Set<String> KEYWORDS = Set.of(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
            "volatile", "while", "true", "false", "null", "var", "record", "yield", "sealed", "permits");

    // 依長度由長到短排列，才能優先吃下最長的運算子
    private static final String[] OPERATORS = {
            ">>>=", "<<=", ">>=", ">>>", "...", "->", "::", "==", "!=", "<=", ">=", "&&", "||", "++", "--",
            "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=", "<<", ">>",
            "=", "<", ">", "!", "~", "?", ":", "+", "-", "*", "/", "&", "|", "^", "%"};

    private static final String PUNCT = "(){}[];,.@";

    private JavaTokens() {}

    public static List<Token> tokenize(String s) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        int n = s.length();
        while (i < n) {
            char c = s.charAt(i);
            int start = i;
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (s.startsWith("//", i)) {
                i = indexOrEnd(s, "\n", i);
                tokens.add(token(Kind.COMMENT, s, start, i));
            } else if (s.startsWith("/*", i)) {
                int close = s.indexOf("*/", i + 2);
                i = close < 0 ? n : close + 2;
                tokens.add(token(Kind.COMMENT, s, start, i));
            } else if (s.startsWith("\"\"\"", i)) {
                int close = s.indexOf("\"\"\"", i + 3);
                i = close < 0 ? n : close + 3;
                tokens.add(token(Kind.STRING, s, start, i));
            } else if (c == '"' || c == '\'') {
                i = endOfQuoted(s, i, c);
                tokens.add(token(c == '"' ? Kind.STRING : Kind.CHAR, s, start, i));
            } else if (Character.isDigit(c)) {
                while (i < n && (Character.isLetterOrDigit(s.charAt(i)) || s.charAt(i) == '_' || s.charAt(i) == '.')) {
                    i++;
                }
                tokens.add(token(Kind.NUMBER, s, start, i));
            } else if (Character.isJavaIdentifierStart(c)) {
                while (i < n && Character.isJavaIdentifierPart(s.charAt(i))) {
                    i++;
                }
                String word = s.substring(start, i);
                tokens.add(new Token(KEYWORDS.contains(word) ? Kind.KEYWORD : Kind.IDENT, start, i, word));
            } else if (PUNCT.indexOf(c) >= 0) {
                i++;
                tokens.add(token(Kind.PUNCT, s, start, i));
            } else {
                String op = operatorAt(s, i);
                i += op != null ? op.length() : 1;
                tokens.add(token(op != null ? Kind.OPERATOR : Kind.OTHER, s, start, i));
            }
        }
        return tokens;
    }

    private static Token token(Kind kind, String s, int start, int end) {
        return new Token(kind, start, end, s.substring(start, end));
    }

    private static int indexOrEnd(String s, String needle, int from) {
        int idx = s.indexOf(needle, from);
        return idx < 0 ? s.length() : idx;
    }

    /** 未結尾的字串在行尾截斷，避免一個漏掉的引號把後面整段都染成字串。 */
    private static int endOfQuoted(String s, int i, char quote) {
        int j = i + 1;
        while (j < s.length()) {
            char c = s.charAt(j);
            if (c == '\\') {
                j += 2;
            } else if (c == quote) {
                return j + 1;
            } else if (c == '\n') {
                return j;
            } else {
                j++;
            }
        }
        return s.length();
    }

    private static String operatorAt(String s, int i) {
        for (String op : OPERATORS) {
            if (s.startsWith(op, i)) {
                return op;
            }
        }
        return null;
    }
}
