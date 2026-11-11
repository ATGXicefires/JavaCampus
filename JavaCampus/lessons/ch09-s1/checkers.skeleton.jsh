interface Checker {
    boolean check(String typed);
}

class ExactChecker implements Checker {
    String answer;

    ExactChecker(String answer) {
        this.answer = answer;
    }

    public boolean check(String typed) {
        return typed.____().equals(answer);
    }
}

class OptionChecker ____ Checker {
    String letter;

    OptionChecker(String letter) {
        this.letter = letter;
    }

    public boolean check(String typed) {
        return typed.equals(letter);
    }
}
