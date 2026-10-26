public class FillQuestion extends Question {
    FillQuestion(String text, String answer) {
        super(text, answer);
    }

    @Override
    boolean check(String typed) {
        return typed.replace(" ", "").equals(answer);
    }
}
