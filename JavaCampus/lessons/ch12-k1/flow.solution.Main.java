import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<Drawable> flow = new ArrayList<>();
        flow.add(new Screen("首頁"));
        flow.add(new Screen("課程"));
        flow.add(new Screen("答題"));
        flow.add(new Screen("成績"));
        for (Drawable d : flow) {
            d.draw();
        }
    }
}
