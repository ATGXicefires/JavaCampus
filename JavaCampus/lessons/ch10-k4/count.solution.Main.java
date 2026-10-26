import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<String> screens = new ArrayList<>();
        screens.add("首頁");
        screens.add("課程列表");
        screens.add("答題");
        screens.add("成績");
        ArrayList<String> features = Features.list();
        System.out.println("畫面 " + screens.size() + " 個，功能 " + features.size() + " 個");
    }
}
