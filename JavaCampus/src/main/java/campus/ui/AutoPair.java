package campus.ui;

/**
 * 括號和引號成對出現，用起來跟一般 IDE 一樣：打左邊那個會順便補上右邊，游標停在中間；
 * 右邊那個已經在游標後面時再打一次只是跳過去；選了一段字再打，就把它包起來。
 */
final class AutoPair {

    /** 把 [start, end) 換成 {@code insert}，再選取 [anchor, caret)；沒有要選東西時兩個一樣。 */
    record Edit(int start, int end, String insert, int anchor, int caret) {
    }

    private static final String OPEN = "([{\"'";
    private static final String CLOSE = ")]}\"'";
    /** 後面緊貼著名字或數字時多半是要把它括起來，右邊那個玩家會自己打在對的地方，這時候不補。 */
    private static final String CLOSE_BEFORE = ")]};,.:=> \t\n";

    private AutoPair() {
    }

    /** 選取範圍 [start, end) 上打了 {@code c}；回傳 null 表示照平常打一個字就好。 */
    static Edit typed(String text, int start, int end, char c) {
        int open = OPEN.indexOf(c);
        if (start < end) {
            return open < 0 ? null
                    : new Edit(start, end, c + text.substring(start, end) + CLOSE.charAt(open), start + 1, end + 1);
        }
        char next = start < text.length() ? text.charAt(start) : '\n';
        boolean literal = CodeCompletion.inLiteral(text, start);
        if (literal && start > 0 && text.charAt(start - 1) == '\\') {
            return null;
        }
        // 引號的左右長得一樣，要在字串裡面才算是右邊那個
        boolean closes = isQuote(c) ? literal : CLOSE.indexOf(c) >= 0;
        if (next == c && closes) {
            return new Edit(start, start, "", start + 1, start + 1);
        }
        // 字串裡打的引號是要把它收起來，括號是文字的一部分
        if (open < 0 || literal || CLOSE_BEFORE.indexOf(next) < 0) {
            return null;
        }
        return new Edit(start, start, "" + c + CLOSE.charAt(open), start + 1, start + 1);
    }

    /** 游標夾在一對空的括號或引號中間，Backspace 要兩邊一起刪。 */
    static boolean deletesPair(String text, int caret) {
        if (caret == 0 || caret >= text.length()) {
            return false;
        }
        char before = text.charAt(caret - 1);
        int open = OPEN.indexOf(before);
        if (open < 0 || text.charAt(caret) != CLOSE.charAt(open)) {
            return false;
        }
        // 「"a"|"b"」游標兩邊是兩段字串的頭尾，不是空的一對
        return !isQuote(before) || CodeCompletion.inLiteral(text, caret);
    }

    private static boolean isQuote(char c) {
        return c == '"' || c == '\'';
    }
}
