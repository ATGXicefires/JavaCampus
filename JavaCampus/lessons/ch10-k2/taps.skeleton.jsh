import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<String> screens = new ArrayList<>();
        screens.add("首頁");
        screens.add("課程列表");
        screens.add("答題");
        screens.add("成績");
        System.out.println("共 " + screens.size() + " 個畫面");
        for (String s : screens) {
            System.out.println("- " + s);
        }
    }

    static int totalTaps(ArrayList<____> taps) {
        int sum = 0;
        for (int t : taps) {
            sum += ____;
        }
        return sum;
    }
}
