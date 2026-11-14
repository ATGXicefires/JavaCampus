____
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ArrayList<Drawable> flow = new ArrayList<>();
        ____
        LessonFlow lessonFlow = new LessonFlow(List.of("變數入門", "語法基礎", "條件判斷", "迴圈練習"));
        List<String> completed = List.of("變數入門");
        System.out.println("剩餘關卡：" + lessonFlow.pending(completed));
        System.out.println("完成率：" + lessonFlow.completionRate(completed) + "%");
    }
}
