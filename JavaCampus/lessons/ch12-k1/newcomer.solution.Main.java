import java.util.ArrayList;

public class Main {
    static final String RETRY = "滷肉：「沒關係，再跑一次！」";
    static final String PASS = "分號：「⋯⋯恭喜。」";

    public static void main(String[] args) {
        ArrayList<Drawable> flow = new ArrayList<>();
        flow.add(new Screen("首頁"));
        flow.add(new Screen("課程"));
        flow.add(new Screen("答題"));
        flow.add(new Screen("成績"));
        for (Drawable d : flow) {
            d.draw();
        }
        String[] typed = {"選晚餐", " 選午餐"};
        Checker checker = new ExactChecker("選午餐");
        for (String t : typed) {
            if (checker.check(t)) {
                System.out.println(PASS);
            } else {
                System.out.println(RETRY);
            }
        }
    }
}
