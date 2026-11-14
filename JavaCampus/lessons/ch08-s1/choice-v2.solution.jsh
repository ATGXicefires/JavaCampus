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

    @Override
    boolean check(String typed) {
        if (typed == null || options == null || correctIndex < 0 || correctIndex >= options.length) {
            return false;
        }
        String t = typed.trim();
        if (t.isEmpty()) {
            return false;
        }
        if (t.length() == 1) {
            int idx = Character.toUpperCase(t.charAt(0)) - 'A';
            if (idx >= 0 && idx < options.length && idx == correctIndex) {
                return true;
            }
        }
        return super.check(t);
    }
}

ChoiceQuestion q = new ChoiceQuestion("口袋剩 40 元，午餐吃什麼？", new String[]{"吃牛排", "吃滷肉飯"}, 1, 10);
