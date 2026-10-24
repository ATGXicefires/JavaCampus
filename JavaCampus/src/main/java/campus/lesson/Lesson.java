package campus.lesson;

import java.util.List;
import java.util.Optional;

/**
 * 劇情裡的一道程式題。
 *
 * @param route   哪個視角的題目（學長／學妹），重設那個視角的進度時一起清掉
 * @param partner 寫程式時誰在旁邊，對應 {@code reactions.json} 的鍵
 * @param notes   過關時解鎖的筆記條目
 */
public record Lesson(String id, String title, String route, String partner, List<String> notes,
                     List<SetupVar> vars, List<Step> steps) {

    public Lesson {
        notes = notes == null ? List.of() : List.copyOf(notes);
        vars = vars == null ? List.of() : List.copyOf(vars);
        steps = steps == null ? List.of() : List.copyOf(steps);
    }

    public Optional<Step> step(String stepId) {
        return steps.stream().filter(s -> s.id().equals(stepId)).findFirst();
    }

    /** 驗證器快取參考解答用的鍵。 */
    public String key(Step step) {
        return id + "/" + step.id();
    }

    Lesson withSteps(List<Step> newSteps) {
        return new Lesson(id, title, route, partner, notes, vars, newSteps);
    }
}
