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

class ChoiceQuestion extends Question {
    String letter;

    ChoiceQuestion(String text, String answer, String letter) {
        super(text, answer);
        this.letter = letter;
    }

    // 覆寫 check：打對選項代號（letter）或完整答案（answer）都算對
}

ChoiceQuestion q = new ChoiceQuestion("口袋剩 40 元，午餐吃什麼？A. 吃牛排 B. 吃滷肉飯", "吃滷肉飯", "B");
