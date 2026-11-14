import java.util.ArrayList;

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
}
