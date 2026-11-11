interface Checker {
    boolean check(String typed);
}

class ExactChecker implements Checker {
    String answer;

    ExactChecker(String answer) {
        this.answer = answer;
    }

    public boolean check(String typed) {
        return typed.trim().equals(answer);
    }
}

class OptionChecker implements Checker {
    String letter;

    OptionChecker(String letter) {
        this.letter = letter;
    }

    public boolean check(String typed) {
        return typed.equals(letter);
    }
}

static String judge(____ c, String typed) {
    if (c.check(typed)) {
        return "答對了";
    }
    return "再試一次";
}
