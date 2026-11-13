import java.util.ArrayList;
import java.util.HashSet;

public class QuizSession {
    private final ArrayList<Question> questions;
    private final HashSet<Integer> solved = new HashSet<>();

    public QuizSession(ArrayList<Question> questions) {
        this.questions = questions == null ? new ArrayList<>() : new ArrayList<>(questions);
    }

    public String submit(int index, String typed) {
        ____
    }

    public int correctCount() {
        ____
    }

    public int score() {
        ____
    }
}
