public class Question {
    String text;
    String answer;

    Question(String text, String answer) {
        this.text = text;
        answer = answer;
    }

    boolean check(String typed) {
        return typed.trim().equals(answer);
    }
}
