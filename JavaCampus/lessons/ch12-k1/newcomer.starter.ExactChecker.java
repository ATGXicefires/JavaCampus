public class ExactChecker implements Checker {
    String answer;

    public ExactChecker(String answer) {
        this.answer = answer;
    }

    public boolean check(String typed) {
        return typed != null && typed.trim().equals(answer);
    }
}
