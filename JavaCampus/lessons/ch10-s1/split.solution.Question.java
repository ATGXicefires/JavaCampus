public class Question implements Checker {
    String text;
    String answer;

    Question(String text, String answer) {
        this.text = text;
        this.answer = answer;
    }

    public boolean check(String typed) {
        return typed.trim().equals(answer);
    }
}
