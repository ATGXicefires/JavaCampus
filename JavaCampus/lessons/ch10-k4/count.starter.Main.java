import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<String> screens = new ArrayList<>();
        screens.add("首頁");
        screens.add("課程列表");
        screens.add("答題");
        screens.add("成績");
        ArrayList<String> features = Features.list();
        // 用 screens 和 features 的 size() 印出 畫面 4 個，功能 5 個
    }
}
