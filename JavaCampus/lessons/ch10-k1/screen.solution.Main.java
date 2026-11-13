public class Main {
    public static String openTab(Screen[] tabs, String title) {
        if (tabs == null || title == null || title.isBlank()) {
            return "未註冊頁籤";
        }
        String target = title.trim();
        for (int i = 0; i < tabs.length; i++) {
            Screen s = tabs[i];
            if (s != null && s.getTitle().equals(target)) {
                if (!s.open()) {
                    return "尚未解鎖：" + s.getTitle();
                }
                return s.render();
            }
        }
        return "未註冊頁籤";
    }

    public static int totalVisits(Screen[] tabs) {
        if (tabs == null) {
            return 0;
        }
        int sum = 0;
        for (int i = 0; i < tabs.length; i++) {
            if (tabs[i] != null) {
                sum += tabs[i].getVisitCount();
            }
        }
        return sum;
    }

    public static int visitAfterSequence(Screen[] tabs, String[] sequence) {
        if (sequence != null) {
            for (int i = 0; i < sequence.length; i++) {
                openTab(tabs, sequence[i]);
            }
        }
        return totalVisits(tabs);
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
