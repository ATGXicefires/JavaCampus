public class Main {
    public static String openTab(Screen[] tabs, String title) {
        if (tabs == null || title == null || title.isBlank()) {
            return "未註冊頁籤";
        }
        String target = title.trim();
        for (int i = 0; i < tabs.length; i++) {
            Screen s = tabs[i];
            if (s != null && s.getTitle().equals(target)) {
                if (!s.select()) {
                    return "未解鎖：" + s.getTitle();
                }
                return s.status();
            }
        }
        return "未註冊頁籤";
    }

    public static int totalClicks(Screen[] tabs) {
        if (tabs == null) {
            return 0;
        }
        int sum = 0;
        for (int i = 0; i < tabs.length; i++) {
            if (tabs[i] != null) {
                sum += tabs[i].getClicks();
            }
        }
        return sum;
    }

    public static int clicksAfterSequence(Screen[] tabs, String[] sequence) {
        if (sequence != null) {
            for (int i = 0; i < sequence.length; i++) {
                openTab(tabs, sequence[i]);
            }
        }
        return totalClicks(tabs);
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
