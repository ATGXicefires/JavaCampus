public class Question {
    private String prompt;
    protected String answer;

    public Question(String prompt, String answer) {
        this.prompt = prompt;
        answer = answer;
    }

    public String getPrompt() {
        return prompt;
    }

    public boolean check(String typed) {
        return typed.trim() == answer;
    }
}
