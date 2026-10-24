package detective.engine;

import java.util.Collection;

final class Edits {

    private Edits() {}

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
