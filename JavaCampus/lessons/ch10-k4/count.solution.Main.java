import java.util.ArrayList;

public class Main {
    public static ArrayList<String> uniqueOf(ArrayList<String> items) {
        ArrayList<String> result = new ArrayList<>();
        if (items == null) {
            return result;
        }
        for (String item : items) {
            if (item != null && !item.isBlank()) {
                String clean = item.trim();
                if (!result.contains(clean)) {
                    result.add(clean);
                }
            }
        }
        return result;
    }

    public static ArrayList<String> duplicatesOf(ArrayList<String> items) {
        ArrayList<String> seen = new ArrayList<>();
        ArrayList<String> dups = new ArrayList<>();
        if (items == null) {
            return dups;
        }
        for (String item : items) {
            if (item != null && !item.isBlank()) {
                String clean = item.trim();
                if (seen.contains(clean)) {
                    if (!dups.contains(clean)) {
                        dups.add(clean);
                    }
                } else {
                    seen.add(clean);
                }
            }
        }
        return dups;
    }

    public static void main(String[] args) {
        ArrayList<String> screens = new ArrayList<>();
        screens.add("首頁");
        screens.add("課程列表");
        screens.add("答題");
        screens.add("成績");

        ArrayList<String> rawFeatures = Features.list();
        ArrayList<String> uniqueFeatures = uniqueOf(rawFeatures);
        ArrayList<String> duplicateFeatures = duplicatesOf(rawFeatures);

        System.out.println("畫面 " + screens.size() + " 個，功能不重複 " + uniqueFeatures.size() + " 個");
        System.out.println("重複註冊：" + duplicateFeatures);
    }
}
