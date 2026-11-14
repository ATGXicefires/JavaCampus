public class FillQuestion extends Question {
    public FillQuestion(String prompt, String answer) {
        super(prompt, answer);
    }

    @Override
    public boolean check(String typed) {
        if (typed == null || answer == null) {
            return false;
        }
        return typed.replace(" ", "").trim().equals(answer.replace(" ", "").trim());
    }
}
