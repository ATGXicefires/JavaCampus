import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        System.out.println("== 製作團隊 ==");
        String[] roles = {"介面設計", "程式", "程式", "測試"};
        String[] names = {"梁亭瑄", "吳二弘", "林學長", "燕韋芸"};
        ArrayList<Credit> credits = new ArrayList<>();
        for (int i = 0; i < roles.length; i++) {
            credits.add(new Credit(roles[i], names[i]));
        }
        for (Credit c : credits) {
            System.out.println(c);
        }
        System.out.println("程式組：" + findByRole(credits, "程式"));

        String[] order = {"阿嬤", "燕韋芸", "李有成", "王葉聖老師"};
        String[] reasons = {"每天的菜", "478 次的統計", "偷拍的照片", "那一次抽籤"};
        HashMap<String, String> thanks = new HashMap<>();
        for (int i = 0; i < order.length; i++) {
            thanks.put(order[i], reasons[i]);
        }
        for (String line : formatThanksInOrder(thanks, order)) {
            System.out.println(line);
        }
    }

    public static ArrayList<String> findByRole(ArrayList<Credit> credits, String role) {
        ArrayList<String> out = new ArrayList<>();
        if (credits == null || role == null || role.isBlank()) {
            return out;
        }
        String target = role.trim();
        for (Credit c : credits) {
            if (c != null && c.role().equals(target)) {
                out.add(c.name());
            }
        }
        return out;
    }

    public static String lookupThank(HashMap<String, String> map, String who) {
        if (map == null || map.isEmpty() || who == null || who.isBlank()) {
            return "無特別感謝紀錄";
        }
        String key = who.trim();
        String reason = map.get(key);
        if (reason == null || reason.isBlank()) {
            return "查無此人：" + key;
        }
        return "特別感謝 " + key + "（" + reason.trim() + "）";
    }

    public static ArrayList<String> formatThanksInOrder(HashMap<String, String> map, String[] order) {
        ArrayList<String> out = new ArrayList<>();
        if (map == null || map.isEmpty() || order == null) {
            return out;
        }
        for (String who : order) {
            if (who != null && !who.isBlank()) {
                String reason = map.get(who.trim());
                if (reason != null && !reason.isBlank()) {
                    out.add(lookupThank(map, who));
                }
            }
        }
        return out;
    }
}
