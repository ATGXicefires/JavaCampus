package detective.content;

import java.util.List;
import java.util.Optional;

public record CaseDef(String id, String title, List<String> concepts, String summary, Newspaper newspaper,
                      List<Line> briefing, String openQuestion, List<SetupVar> vars, List<Evidence> evidence,
                      List<Step> steps, Conclusion conclusion, List<String> explanation) {

    public CaseDef {
        concepts = concepts == null ? List.of() : List.copyOf(concepts);
        briefing = briefing == null ? List.of() : List.copyOf(briefing);
        vars = vars == null ? List.of() : List.copyOf(vars);
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
        steps = steps == null ? List.of() : List.copyOf(steps);
        explanation = explanation == null ? List.of() : List.copyOf(explanation);
    }

    public Optional<Step> step(String stepId) {
        return steps.stream().filter(s -> s.id().equals(stepId)).findFirst();
    }

    /** 驗證器快取參考解答用的鍵。 */
    public String key(Step step) {
        return id + "/" + step.id();
    }

    CaseDef withSteps(List<Step> newSteps) {
        return new CaseDef(id, title, concepts, summary, newspaper, briefing, openQuestion, vars, evidence,
                newSteps, conclusion, explanation);
    }
}
