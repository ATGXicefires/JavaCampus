public class FillQuestion extends Question {
    FillQuestion(String text, String answer) {
        super(text, answer);
    }

    boolean chek(String typed) {
        return typed.replace(" ", "").equals(answer);
    }
}
