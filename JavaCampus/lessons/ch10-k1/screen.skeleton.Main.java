public class Main {
    public static String openTab(Screen[] tabs, String title) {
        ____
    }

    public static int totalVisits(Screen[] tabs) {
        ____
    }

    public static int visitAfterSequence(Screen[] tabs, String[] sequence) {
        ____
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
