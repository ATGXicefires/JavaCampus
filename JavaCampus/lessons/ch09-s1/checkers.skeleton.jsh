class ExactChecker implements ____ {
    String answer;
    boolean ignoreCase;

    ExactChecker(String answer, boolean ignoreCase) {
        this.answer = answer;
        this.ignoreCase = ignoreCase;
    }

    public boolean check(String typed) {
        if (answer == null || typed == null) {
            return false;
        }
        ____
    }
}

class RangeChecker implements ____ {
    double target;
    double tolerance;

    RangeChecker(double target, double tolerance) {
        this.target = target;
        this.tolerance = Math.abs(tolerance);
    }

    public boolean check(String typed) {
        ____
    }
}

class KeywordChecker implements ____ {
    String[] keywords;

    KeywordChecker(String[] keywords) {
        ____
    }

    public boolean check(String typed) {
        ____
    }
}
