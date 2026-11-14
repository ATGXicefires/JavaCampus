import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static ArrayList<String> missing(ArrayList<String> needs, ArrayList<String> have) {
        ____
    }

    public static void main(String[] args) {
        ArrayList<String> screens = new ArrayList<>();
        screens.add("首頁");
        screens.add("課程列表");
        screens.add("答題");
        screens.add("成績");

        HashMap<String, String> needs = new HashMap<>();
        needs.put("首頁", "登入");
        needs.put("課程列表", "課程");
        needs.put("答題", "題目");
        needs.put("成績", "計分");

        ArrayList<String> neededList = new ArrayList<>();
        for (String screen : screens) {
            if (needs.containsKey(screen)) {
                neededList.add(needs.get(screen));
            }
        }

        ArrayList<String> rawFeatures = Features.list();
        ArrayList<String> uniqueFeatures = Reconciler.cleanUnique(rawFeatures);

        System.out.println("畫面 " + screens.size() + " 個，功能不重複 " + uniqueFeatures.size() + " 個");
        System.out.println("缺漏：" + Reconciler.missingFeatures(neededList, rawFeatures));
        System.out.println("重複：" + Reconciler.duplicateItems(rawFeatures));
        System.out.println("未用到：" + Reconciler.unusedFeatures(neededList, rawFeatures));
    }
}
