import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        String[] texts = {"印出一行字要用哪個方法？", "整數的型別是什麼？", "重複做同一件事用什麼？", "比較兩個字串的內容用什麼？", "二選一用什麼？"};
        String[] answers = {"println", "int", "for", "equals", "if"};
        ArrayList<Question> quiz = new ArrayList<>();
        for (int i = 0; i < texts.length; i++) {
            quiz.add(new Question(texts[i], answers[i]));
        }
        for (int i = 0; i < quiz.size(); i++) {
            System.out.println("第 " + (i + 1) + " 題：" + quiz.get(i).text);
        }
        String[] typed = {" println", "int", "while", "equals ", "if"};
        int correct = 0;
        for (int i = 0; i < quiz.size(); i++) {
            if (typed[i].trim().equals(quiz.get(i).answer)) {
                correct++;
            }
        }
        System.out.println("答對 " + correct + " 題");
        if (correct >= 3) {
            System.out.println("★ 完成");
        }
    }
}
