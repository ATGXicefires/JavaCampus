class Question {
    String text;
    String answer;
    int points;

    Question(String text, String answer, int points) {
        this.text = text;
        this.answer = answer;
        this.points = points <= 0 ? 1 : points;
    }

    boolean check(String typed) {
        if (typed == null || answer == null) {
            return false;
        }
        return typed.trim().equals(answer);
    }

    int score(String typed) {
        return check(typed) ? points : 0;
    }
}

class ChoiceQuestion extends Question {
    String[] options;
    int correctIndex;

    ChoiceQuestion(String text, String[] options, int correctIndex, int points) {
        super(text, (options != null && correctIndex >= 0 && correctIndex < options.length) ? options[correctIndex] : "", points);
        this.options = options;
        this.correctIndex = correctIndex;
    }

    // 用 @Override 覆寫 boolean check(String typed)：處理選項索引邊界、字母代號（不分大小寫）與完整文字比對
}

ChoiceQuestion q = new ChoiceQuestion("口袋剩 40 元，午餐吃什麼？", new String[]{"吃牛排", "吃滷肉飯"}, 1, 10);
