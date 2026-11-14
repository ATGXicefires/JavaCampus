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

        // 實作 filterWithNotes 並逐行印出有更新項目的版本
    }

    public static ArrayList<Release> filterWithNotes(List<Release> releases) {
        // 回傳不為 null 且 items() 非空的版本清單
        return new ArrayList<>();
    }
}
