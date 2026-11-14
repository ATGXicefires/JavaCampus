public class FillQuestion extends Question {
    public FillQuestion(String prompt, String answer) {
        super(prompt, answer);
    }

    public boolean check(String typed, boolean ignoreSpace) {
        return typed.replace(" ", "").equals(answer);
    }
}
