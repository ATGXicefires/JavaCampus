import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ArrayList<Release> releases = new ArrayList<>();
        releases.add(new Release("v0.9.0", "2026-03-10", List.of("前後端合併", "基礎單元測試")));
        releases.add(new Release("v1.10.0", "2026-06-01", List.of("效能優化", "補齊圖鑑筆記")));
        releases.add(new Release("v1.0.0", "2026-04-01", List.of("", "  ")));
        releases.add(new Release("v1.2.0", "2026-04-20", List.of("三種題型", "進度條元件")));
        releases.add(new Release("v1.3.0", "2026-05-15", List.of("發表前修正")));

        for (Release r : filterWithNotes(releases)) {
            System.out.println(r);
        }
        System.out.println("最新：" + latestSummary(releases));
    }

    public static ArrayList<Release> filterWithNotes(List<Release> releases) {
        ArrayList<Release> out = new ArrayList<>();
        if (releases == null) {
            return out;
        }
        for (Release r : releases) {
            if (r != null && !r.items().isEmpty()) {
                out.add(r);
            }
        }
        return out;
    }

    public static int compareVersions(String v1, String v2) {
        String[] p1 = parseVersionParts(v1);
        String[] p2 = parseVersionParts(v2);
        int len = Math.max(p1.length, p2.length);
        for (int i = 0; i < len; i++) {
            int n1 = i < p1.length ? parsePart(p1[i]) : 0;
            int n2 = i < p2.length ? parsePart(p2[i]) : 0;
            if (n1 != n2) {
                return Integer.compare(n1, n2);
            }
        }
        return 0;
    }

    private static String[] parseVersionParts(String v) {
        if (v == null || v.isBlank()) {
            return new String[]{"0"};
        }
        String s = v.trim();
        if (s.startsWith("v") || s.startsWith("V")) {
            s = s.substring(1);
        }
        return s.isEmpty() ? new String[]{"0"} : s.split("\\.");
    }

    private static int parsePart(String part) {
        if (part == null || part.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(part.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static Release findLatest(List<Release> releases) {
        if (releases == null) {
            return null;
        }
        Release best = null;
        for (Release r : releases) {
            if (r == null) {
                continue;
            }
            if (best == null || compareVersions(r.version(), best.version()) > 0) {
                best = r;
            }
        }
        return best;
    }

    public static String latestSummary(List<Release> releases) {
        Release latest = findLatest(releases);
        return latest == null ? "尚無版本紀錄" : latest.toString();
    }
}
