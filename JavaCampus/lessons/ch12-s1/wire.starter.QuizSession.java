import java.util.ArrayList;

public class QuizSession {
    private final ArrayList<Question> questions;

    public QuizSession(ArrayList<Question> questions) {
        this.questions = questions == null ? new ArrayList<>() : new ArrayList<>(questions);
    }

    public String submit(int index, String typed) {
        // 1. 無效題號回傳 "無效題號"
        // 2. 用 ExactChecker 判斷，答錯回傳 "再試一次"
        // 3. 第一次答對標記並回傳 "答對了"；重複答對不重複計分並回傳 "已答對過"
        return "";
    }

    public int correctCount() {
        return 0;
    }

    public int score() {
        return 0;
    }
}
