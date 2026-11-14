import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static ArrayList<String> cleanScreens(ArrayList<String> raw) {
        ArrayList<String> result = new ArrayList<>();
        if (raw == null) {
            return result;
        }
        for (int i = 0; i < raw.size(); i++) {
            String item = raw.get(i);
            if (item != null && !item.isBlank()) {
                String clean = item.trim();
                if (!result.contains(clean)) {
                    result.add(clean);
                }
            }
        }
        return result;
    }

    public static ArrayList<String> filterByKeyword(ArrayList<String> screens, String keyword) {
        ____
    }

    public static void main(String[] args) {
        ArrayList<String> raw = new ArrayList<>(Arrays.asList("首頁", " ", "課程列表", "首頁", "答題", "", "成績"));
        ArrayList<String> screens = cleanScreens(raw);
        System.out.println("共 " + screens.size() + " 個畫面");
        for (String s : screens) {
            ____
        }
        ArrayList<String> matched = filterByKeyword(screens, "答");
        System.out.println("符合「答」：" + matched.size() + " 個");
    }
}
