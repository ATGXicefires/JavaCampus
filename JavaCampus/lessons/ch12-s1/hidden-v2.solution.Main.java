import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        String[] texts = {"if-else 像什麼？", "印出一行字用什麼？", "整數的型別是什麼？"};
        String[] answers = {"選午餐", "println", "int"};
        String[] typed = {" 選午餐", "print", "int"};
        Records records = new Records();
        records.record("第 1 課", 33);
        new Screen("第 1 課").show();
        ArrayList<Question> quiz = new ArrayList<>();
        for (int i = 0; i < texts.length; i++) {
            quiz.add(new Question(texts[i], answers[i]));
        }
        QuizSession session = new QuizSession(quiz);
        for (int i = 0; i < quiz.size(); i++) {
            System.out.println("第 " + (i + 1) + " 題：" + session.submit(i, typed[i]));
        }
        records.record("第 1 課", session.score());
        System.out.println("最高分：" + records.bestOf("第 1 課"));
    }

    public static int runQuiz(String[] answers, int[] indices, String[] typed) {
        if (answers == null || answers.length == 0 || indices == null || typed == null) {
            return 0;
        }
        ArrayList<Question> quiz = new ArrayList<>();
        for (int i = 0; i < answers.length; i++) {
            quiz.add(new Question("Q" + (i + 1), answers[i]));
        }
        QuizSession session = new QuizSession(quiz);
        int count = Math.min(indices.length, typed.length);
        for (int i = 0; i < count; i++) {
            session.submit(indices[i], typed[i]);
        }
        return session.score();
    }

    public static String formatReport(String unit, String[] answers, int[] indices, String[] typed, Records records) {
        String label = (unit == null || unit.isBlank()) ? "未命名單元" : unit;
        int score = runQuiz(answers, indices, typed);
        if (records != null) {
            records.record(label, score);
        }
        int best = records == null ? score : records.bestOf(label);
        return label + "：本次 " + score + " 分，最高 " + best + " 分";
    }
}
