import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        System.out.println("== 製作團隊 ==");
        String[] roles = {"介面設計", "程式", "程式", "測試"};
        String[] names = {"梁亭瑄", "吳二弘", "林學長", "燕韋芸"};
        // 用 ArrayList<Credit> 放進所有成員，再用 for-each 印出來
        // 最後印出：程式組： + findByRole(credits, "程式")
    }

    public static ArrayList<String> findByRole(ArrayList<Credit> credits, String role) {
        // 回傳指定職位的成員名字清單
        return new ArrayList<>();
    }
}
