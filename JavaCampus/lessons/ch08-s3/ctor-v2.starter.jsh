class Question {
    String text;
    String answer;

    Question(String text, String answer) {
        this.text = text;
        this.answer = answer;
    }

    boolean check(String typed) {
        if (typed == null || answer == null) {
            return false;
        }
        return typed.trim().equals(answer);
    }
}

class ChoiceQuestion extends Question {
    String[] options;
    int correctIndex;

    ChoiceQuestion(String text, String[] options, int correctIndex) {
        super(text, (options != null && correctIndex >= 0 && correctIndex < options.length) ? options[correctIndex] : "");
        this.options = options;
        this.correctIndex = correctIndex;
    }

    @Override
    boolean check(String typed) {
        if (typed == null || options == null || correctIndex < 0 || correctIndex >= options.length) {
            return false;
        }
        String t = typed.trim();
        if (t.length() == 1) {
            int idx = Character.toUpperCase(t.charAt(0)) - 'A';
            if (idx >= 0 && idx < options.length && idx == correctIndex) {
                return true;
            }
        }
        return super.check(t);
    }
}

// 寫 class NumberQuestion 繼承 Question
// 新增欄位 double target 與 double tolerance
// 寫建構子 NumberQuestion(String text, double target, double tolerance)，透過 super(...) 對接並對 tolerance 防呆
