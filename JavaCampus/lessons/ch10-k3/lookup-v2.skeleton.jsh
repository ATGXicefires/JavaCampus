import java.util.HashMap;

public class Main {
    public static HashMap<String, String> buildTable() {
        HashMap<String, String> initials = new HashMap<>();
        initials.put("yc", "李有成");
        initials.put("ys", "王葉聖");
        initials.put("wy", "燕韋芸");
        initials.put("eh", "吳二弘");
        ____
        return initials;
    }

    public static String lookupOrUnknown(HashMap<String, String> table, String code) {
        if (table == null || code == null || code.isBlank()) {
            return "無效縮寫";
        }
        String key = code.trim().toLowerCase();
        if (table.containsKey(key)) {
            return key + " 是 " + table.get(key);
        }
        return key + " 查無此人";
    }

    public static String expandTags(HashMap<String, String> table, String sentence) {
        ____
    }

    public static String findCode(HashMap<String, String> table, String name) {
        ____
    }

    public static void main(String[] args) {
        HashMap<String, String> initials = buildTable();
        System.out.println(lookupOrUnknown(initials, "yc"));
        System.out.println(lookupOrUnknown(initials, "tx"));
        System.out.println(expandTags(initials, "eh 與 tx 討論專題"));
        System.out.println(findCode(initials, "梁亭瑄"));
    }
}
