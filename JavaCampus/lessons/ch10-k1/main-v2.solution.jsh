public class Main {
    public static String formatTab(String title, boolean unlocked) {
        String clean = (title == null || title.isBlank()) ? "未命名" : title.trim();
        return (unlocked ? "[已解鎖] " : "[未解鎖] ") + clean;
    }

    public static void main(String[] args) {
        System.out.println("小白學程式 v0.9");
        System.out.println(formatTab("首頁", true));
        System.out.println(formatTab("進階統計", false));
    }
}
