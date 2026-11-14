public class FillQuestion extends Question {
    public FillQuestion(String prompt, String answer) {
        super(prompt, answer);
    }

    @Override
    public boolean check(____) {
        if (typed == null || answer == null) {
            return false;
        }
        return ____;
    }
}
