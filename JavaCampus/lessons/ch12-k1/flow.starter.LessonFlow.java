import java.util.ArrayList;
import java.util.List;

public class LessonFlow {
    private final ArrayList<String> stages = new ArrayList<>();

    public LessonFlow(List<String> stages) {
        // 若 stages 不為 null，依序加入非 null、非空白且未重複的關卡名稱
    }

    public ArrayList<String> pending(List<String> completed) {
        // 回傳尚未出現在 completed 中的關卡清單
        return new ArrayList<>();
    }

    public int completionRate(List<String> completed) {
        // 計算完成率（已完成數 * 100 / 總數），總數為 0 回傳 0
        return 0;
    }
}
