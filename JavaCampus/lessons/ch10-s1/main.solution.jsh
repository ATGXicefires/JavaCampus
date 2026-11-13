public class Main {
    public static String tabSummary(String title, int items, boolean unlocked) {
        String cleanTitle = (title == null || title.isBlank()) ? "未命名" : title.trim();
        int cleanItems = items < 0 ? 0 : items;
        return (unlocked ? "[開啟] " : "[鎖定] ") + cleanTitle + " (" + cleanItems + " 項)";
    }

    public static void main(String[] args) {
        String name = "小白學程式";
        String version = "v0.9";
        System.out.println(name + " " + version);
        System.out.println(tabSummary("題庫", 5, true));
        System.out.println(tabSummary("", -3, false));
    }
}
