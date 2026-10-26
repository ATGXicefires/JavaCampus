class Question {
    String text;
    String answer;

    Question(String text, String answer) {
        this.text = text;
        this.answer = answer;
    }

    boolean check(String typed) {
        return typed.equals(answer);
    }
}

// class NumberQuestion 繼承 Question
// 建構子 NumberQuestion(String text, String answer)：用 super(text, answer) 交給父類別
