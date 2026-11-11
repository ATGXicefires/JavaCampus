import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        System.out.println("== 製作團隊 ==");
        ArrayList<Credit> credits = new ArrayList<>();
        credits.add(new Credit("介面設計", "梁亭瑄"));
        credits.add(new Credit("程式", "吳二弘"));
        for (Credit c : credits) {
            System.out.println(c);
        }
    }
}
