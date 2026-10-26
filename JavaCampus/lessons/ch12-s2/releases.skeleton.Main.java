import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        String[] versions = {"v0.1", "v0.2", "v0.3", "v0.4", "v0.5", "v0.9", "v0.99", "v1.0"};
        String[] months = {"11月", "12月", "1月", "3月", "4月", "5月", "6月", "6月"};
        String[] notes = {"校慶試玩版", "期中報告", "上學期進度", "三種題型", "畫面元件", "前後端合併", "最後調整", "專題發表"};
        ArrayList<Release> releases = new ArrayList<>();
        for (int i = 0; i < versions.length; i++) {
            releases.____(new Release(versions[i], months[i], notes[i]));
        }
        for (Release r : releases) {
            System.out.println(r);
        }
    }
}
