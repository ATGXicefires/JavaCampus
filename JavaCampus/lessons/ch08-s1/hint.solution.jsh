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

    String hint() {
        return "答案有 " + answer.length() + " 個字";
    }
}

class ChoiceQuestion extends Question {
    String letter;

    ChoiceQuestion(String text, String answer, String letter) {
        super(text, answer);
        this.letter = letter;
    }

    boolean check(String typed) {
        return typed.equals(letter) || typed.equals(answer);
    }
}

ChoiceQuestion q = new ChoiceQuestion("口袋剩 40 元，午餐吃什麼？A. 吃牛排 B. 吃滷肉飯", "吃滷肉飯", "B");
