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
        ____
    }

    public static String formatReport(String unit, String[] answers, int[] indices, String[] typed, Records records) {
        ____
    }
}
