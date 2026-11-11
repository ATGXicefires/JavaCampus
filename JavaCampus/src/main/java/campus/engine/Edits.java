package campus.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

final class Edits {

    /** 把 a 改成 b 的一步。{@code at} 是 a 裡的位置：插入時是插在第 at 個字前面，刪除和替換時是那個字本身。 */
    record Change(Kind kind, int at, char from, char to) {
        enum Kind { INSERT, DELETE, REPLACE }
    }

    private Edits() {}

    /** 把 a 改成 b 最少要哪幾步，照位置排好。 */
    static List<Change> changes(String a, String b) {
        int[][] d = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            d[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            d[0][j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                d[i][j] = Math.min(Math.min(d[i][j - 1] + 1, d[i - 1][j] + 1), d[i - 1][j - 1] + cost);
            }
        }
        List<Change> out = new ArrayList<>();
        int i = a.length();
        int j = b.length();
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && a.charAt(i - 1) == b.charAt(j - 1) && d[i][j] == d[i - 1][j - 1]) {
                i--;
                j--;
            } else if (i > 0 && j > 0 && d[i][j] == d[i - 1][j - 1] + 1) {
                out.add(new Change(Change.Kind.REPLACE, i - 1, a.charAt(i - 1), b.charAt(j - 1)));
                i--;
                j--;
            } else if (i > 0 && d[i][j] == d[i - 1][j] + 1) {
                out.add(new Change(Change.Kind.DELETE, i - 1, a.charAt(i - 1), '\0'));
                i--;
            } else {
                out.add(new Change(Change.Kind.INSERT, i, '\0', b.charAt(j - 1)));
                j--;
            }
        }
        Collections.reverse(out);
        return out;
    }

    static int distance(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] cur = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            prev[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            cur[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] t = prev;
            prev = cur;
            cur = t;
        }
        return prev[b.length()];
    }

    /** 找最接近的候選名稱；只差大小寫的優先，其次是編輯距離在容許範圍內的。 */
    static String nearest(String name, Collection<String> candidates) {
        String best = null;
        int bestDistance = Integer.MAX_VALUE;
        int allowed = name.length() <= 3 ? 1 : 2;
        for (String c : candidates) {
            if (c.equals(name)) {
                continue;
            }
            if (c.equalsIgnoreCase(name)) {
                return c;
            }
            int d = distance(name, c);
            if (d <= allowed && d < bestDistance) {
                best = c;
                bestDistance = d;
            }
        }
        return best;
    }
}
