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

    static double accuracy(Question[] quiz, String[] answers) {
        if (quiz == null || answers == null) {
            return 0.0;
        }
        int validCount = 0;
        int correct = 0;
        for (int i = 0; i < quiz.length; i++) {
            if (quiz[i] != null) {
                validCount++;
                if (i < answers.length && ____) {
                    correct++;
                }
            }
        }
        return validCount == 0 ? 0.0 : (double) correct / validCount;
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
        super(text, String.valueOf(target));
        this.target = target;
        this.tolerance = (Double.isNaN(tolerance) || tolerance < 0) ? 0.0 : tolerance;
    }

    @Override
    boolean check(String typed) {
        if (typed == null || typed.isBlank()) {
            return false;
        }
        double val;
        try {
            val = Double.parseDouble(typed.trim());
        } catch (NumberFormatException e) {
            return false;
        }
        if (!Double.isFinite(val)) {
            return false;
        }
        return ____;
    }
}
