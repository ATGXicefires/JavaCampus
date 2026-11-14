public class Main {
    public static String openTab(Screen[] tabs, String title) {
        ____
    }

    public static int totalClicks(Screen[] tabs) {
        ____
    }

    public static int clicksAfterSequence(Screen[] tabs, String[] sequence) {
        ____
    }

    public static void main(String[] args) {
        String name = "小白學程式";
        String version = "v0.9";
        System.out.println(name + " " + version);
        Question q = new Question("if-else 像什麼？", "選午餐");
        System.out.println(q.text + " " + q.check(" 選午餐 "));

        Screen bank = new Screen("題庫", true);
        bank.setFilter("選擇題");
        Screen stats = new Screen("統計", false);
        Screen[] tabs = { bank, stats };

        System.out.println(openTab(tabs, "題庫"));
        System.out.println(openTab(tabs, "統計"));
        System.out.println(openTab(tabs, "不存在"));
        System.out.println("總點擊：" + totalClicks(tabs));
    }
}
