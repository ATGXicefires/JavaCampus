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
        ____
    }

    public static Release findLatest(List<Release> releases) {
        ____
    }

    public static String latestSummary(List<Release> releases) {
        ____
    }
}
