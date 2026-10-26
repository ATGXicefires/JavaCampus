import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        ArrayList<String> screens = new ArrayList<>();
        screens.add("首頁");
        screens.add("課程列表");
        screens.add("答題");
        screens.add("成績");
        ArrayList<String> features = Features.list();
        HashMap<String, String> needs = new HashMap<>();
        needs.put("首頁", "登入");
        needs.put("課程列表", "課程");
        needs.put("答題", "題目");
        needs.put("成績", "計分");
        System.out.println("畫面 " + screens.size() + " 個，功能 " + features.size() + " 個");
        // 用 for-each 找出需要的功能不在 features 裡的畫面
    }
}
