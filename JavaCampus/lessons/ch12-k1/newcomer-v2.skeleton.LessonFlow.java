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
        ____
    }

    public boolean isUnlocked(String target, List<String> completed) {
        ____
    }
}
