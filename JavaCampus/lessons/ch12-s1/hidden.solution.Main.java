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
        int correct = 0;
        for (int i = 0; i < quiz.size(); i++) {
            Checker c = new ExactChecker(quiz.get(i).answer);
            if (c.check(typed[i])) {
                correct++;
                System.out.println("第 " + (i + 1) + " 題：答對了");
            } else {
                System.out.println("第 " + (i + 1) + " 題：再試一次");
            }
        }
        records.record("第 1 課", correct * 100 / quiz.size());
        System.out.println("最高分：" + records.bestOf("第 1 課"));
    }

    static int runQuiz(String[] answers, String[] typed) {
        int correct = 0;
        for (int i = 0; i < answers.length; i++) {
            Checker c = new ExactChecker(answers[i]);
            if (c.check(typed[i])) {
                correct++;
            }
        }
        return correct;
    }
}
