import java.util.List;
// 請補上 ArrayList 的 import

public class Main {
    public static void main(String[] args) {
        // 用 ArrayList<Drawable> 依序放進 首頁、課程、答題、成績 四個畫面並呼叫 draw()

        LessonFlow lessonFlow = new LessonFlow(List.of("變數入門", "語法基礎", "條件判斷", "迴圈練習"));
        List<String> completed = List.of("變數入門");
        System.out.println("剩餘關卡：" + lessonFlow.pending(completed));
        System.out.println("完成率：" + lessonFlow.completionRate(completed) + "%");
    }
}
