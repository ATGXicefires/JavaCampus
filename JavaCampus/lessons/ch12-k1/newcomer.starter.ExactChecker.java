public class ExactChecker implements Checker {
    String answer;

    ExactChecker(String answer) {
        this.answer = answer;
    }

    public boolean check(String typed) {
        return typed.trim().equals(answer);
    }
}
