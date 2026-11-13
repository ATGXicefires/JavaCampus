import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static ArrayList<String> cleanScreens(ArrayList<String> raw) {
        return new ArrayList<>();
    }

    public static void main(String[] args) {
        ArrayList<String> raw = new ArrayList<>(Arrays.asList("首頁", " ", "課程列表", "首頁", "答題", "", "成績"));
        ArrayList<String> screens = cleanScreens(raw);
        System.out.println("共 " + screens.size() + " 個畫面");
    }
}
