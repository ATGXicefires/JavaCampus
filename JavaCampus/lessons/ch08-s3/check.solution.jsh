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

class NumberQuestion extends Question {
    NumberQuestion(String text, String answer) {
        super(text, answer);
    }

    @Override
    boolean check(String typed) {
        String trimmed = typed.trim();
        try {
            Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            return false;
        }
        return super.check(trimmed);
    }
}
