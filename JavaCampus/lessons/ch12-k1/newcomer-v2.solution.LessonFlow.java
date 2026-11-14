import java.util.ArrayList;
import java.util.List;

public class LessonFlow {
    private final ArrayList<String> stages = new ArrayList<>();

    public LessonFlow(List<String> stages) {
        if (stages == null) {
            return;
        }
        for (String stage : stages) {
            if (stage != null && !stage.isBlank() && !this.stages.contains(stage)) {
                this.stages.add(stage);
            }
        }
    }

    public ArrayList<String> pending(List<String> completed) {
        ArrayList<String> out = new ArrayList<>();
        for (String stage : stages) {
            if (completed == null || !completed.contains(stage)) {
                out.add(stage);
            }
        }
        return out;
    }

    public int completionRate(List<String> completed) {
        if (stages.isEmpty()) {
            return 0;
        }
        int done = 0;
        if (completed != null) {
            for (String stage : stages) {
                if (completed.contains(stage)) {
                    done++;
                }
            }
        }
        return done * 100 / stages.size();
    }

    public String nextRecommended(List<String> completed) {
        ArrayList<String> left = pending(completed);
        return left.isEmpty() ? "全部完成" : left.get(0);
    }

    public boolean isUnlocked(String target, List<String> completed) {
        if (target == null) {
            return false;
        }
        int index = stages.indexOf(target);
        if (index < 0) {
            return false;
        }
        if (index == 0) {
            return true;
        }
        return completed != null && completed.contains(stages.get(index - 1));
    }
}
