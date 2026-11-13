import java.util.ArrayList;
import java.util.HashSet;

public class QuizSession {
    private final ArrayList<Question> questions;
    private final HashSet<Integer> solved = new HashSet<>();

    public QuizSession(ArrayList<Question> questions) {
        this.questions = questions == null ? new ArrayList<>() : new ArrayList<>(questions);
    }

    public String submit(int index, String typed) {
        if (index < 0 || index >= questions.size() || questions.get(index) == null) {
            return "無效題號";
        }
        Checker checker = new ExactChecker(questions.get(index).answer);
        if (!checker.check(typed)) {
            return "再試一次";
        }
        if (solved.add(index)) {
            return "答對了";
        }
        return "已答對過";
    }

    public int correctCount() {
        return solved.size();
    }

    public int score() {
        if (questions.isEmpty()) {
            return 0;
        }
        return solved.size() * 100 / questions.size();
    }
}
