import java.util.ArrayList;
import java.util.List;

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
        LessonFlow lessonFlow = new LessonFlow(List.of("變數入門", "語法基礎", "條件判斷", "迴圈練習"));
        List<String> completed = List.of("變數入門");
        System.out.println("剩餘關卡：" + lessonFlow.pending(completed));
        System.out.println("完成率：" + lessonFlow.completionRate(completed) + "%");
    }
}
