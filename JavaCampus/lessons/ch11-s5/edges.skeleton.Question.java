public class Question {
    private String prompt;
    protected String answer;

    public Question(String prompt, String answer) {
        this.prompt = prompt;
        this.answer = answer;
    }

    public String getPrompt() {
        return prompt;
    }

    public boolean check(String typed) {
        if (typed == null || answer == null) {
            return false;
        }
        return typed.trim().equals(answer.trim());
    }
}
