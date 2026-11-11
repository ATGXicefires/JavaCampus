import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        HashMap<String, String> initials = new HashMap<>();
        initials.put("yc", "李有成");
        initials.put("ys", "王葉聖");
        initials.put("wy", "燕韋芸");
        initials.put("eh", "吳二弘");
        System.out.println("yc 是 " + initials.get("yc"));
        initials.put("tx", "梁亭瑄");
        String[] queries = {"tx", "zz"};
        for (String q : queries) {
            if (initials.containsKey(q)) {
                System.out.println(q + " 是 " + initials.get(q));
            } else {
                System.out.println(q + " 查無此人");
            }
        }
    }
}
