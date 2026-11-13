public class Question implements Checker {
    public String text;
    public String answer;

    public Question(String text, String answer) {
        this.text = text;
        this.answer = answer;
    }

    public boolean check(String typed) {
        return typed != null && typed.trim().equals(answer);
    }
}
