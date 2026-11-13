public class Main {
    public static String openTab(Screen[] tabs, String title) {
        // 請實作頁籤查詢、解鎖檢查與切換邏輯
        return "";
    }

    public static int totalVisits(Screen[] tabs) {
        // 請實作非 null 頁籤的總點擊數加總
        return 0;
    }

    public static int visitAfterSequence(Screen[] tabs, String[] sequence) {
        // 請依序呼叫 openTab，最後回傳 totalVisits(tabs)
        return 0;
    }

    public static void main(String[] args) {
        System.out.println("小白學程式 v0.9");
        Screen[] tabs = {
            new Screen("首頁", true),
            new Screen("課程列表", true),
            new Screen("成績統計", false)
        };
        System.out.println(openTab(tabs, "首頁"));
        System.out.println(openTab(tabs, "成績統計"));
        tabs[2].unlock();
        System.out.println(openTab(tabs, "成績統計"));
        System.out.println(openTab(tabs, "設定"));
        System.out.println("總切換：" + totalVisits(tabs));
    }
}
