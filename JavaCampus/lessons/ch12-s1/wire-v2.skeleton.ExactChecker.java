public class ExactChecker implements Checker {
    String answer;

    public ExactChecker(String answer) {
        this.answer = answer;
    }

    public boolean check(String typed) {
        return typed != null && answer != null && typed.trim().equals(answer);
    }
}
