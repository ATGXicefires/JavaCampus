import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        System.out.println("== 製作團隊 ==");
        ArrayList<Credit> credits = new ArrayList<>();
        credits.add(new Credit("介面設計", "梁亭瑄"));
        credits.add(new Credit("程式", "吳二弘"));
        for (Credit c : credits) {
            System.out.println(c);
        }
        HashMap<String, String> thanks = new HashMap<>();
        thanks.put("阿嬤", "每天的菜");
        thanks.put("燕韋芸", "478 次的統計");
        thanks.put("李有成", "偷拍的照片");
        thanks.put("王葉聖老師", "那一次抽籤");
        String[] order = {"阿嬤", "燕韋芸", "李有成", "王葉聖老師"};
        for (String who : order) {
            System.out.println("特別感謝 " + who + "（" + thanks.get(who) + "）");
        }
    }
}
