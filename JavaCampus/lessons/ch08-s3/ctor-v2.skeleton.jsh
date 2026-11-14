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

class NumberQuestion extends Question {
    double target;
    double tolerance;

    NumberQuestion(String text, double target, double tolerance) {
        super(____);
        this.target = target;
        this.tolerance = (Double.isNaN(tolerance) || tolerance < 0) ? 0.0 : tolerance;
    }
}
