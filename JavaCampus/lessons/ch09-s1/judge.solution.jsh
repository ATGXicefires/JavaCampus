interface Checker {
    boolean check(String typed);
}

class ExactChecker implements Checker {
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
        String a = answer.trim();
        String b = typed.trim();
        return ignoreCase ? a.equalsIgnoreCase(b) : a.equals(b);
    }
}

class RangeChecker implements Checker {
    double target;
    double tolerance;

    RangeChecker(double target, double tolerance) {
        this.target = target;
        this.tolerance = Math.abs(tolerance);
    }

    public boolean check(String typed) {
        if (typed == null || typed.trim().isEmpty()) {
            return false;
        }
        if (Double.isNaN(target) || Double.isInfinite(target)) {
            return false;
        }
        try {
            double val = Double.parseDouble(typed.trim());
            if (Double.isNaN(val) || Double.isInfinite(val)) {
                return false;
            }
            return Math.abs(val - target) <= tolerance + 1e-9;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

class KeywordChecker implements Checker {
    String[] keywords;

    KeywordChecker(String[] keywords) {
        if (keywords == null) {
            this.keywords = new String[0];
        } else {
            int count = 0;
            for (int i = 0; i < keywords.length; i++) {
                if (keywords[i] != null && !keywords[i].trim().isEmpty()) {
                    count++;
                }
            }
            this.keywords = new String[count];
            int idx = 0;
            for (int i = 0; i < keywords.length; i++) {
                if (keywords[i] != null && !keywords[i].trim().isEmpty()) {
                    this.keywords[idx++] = keywords[i].trim().toLowerCase();
                }
            }
        }
    }

    public boolean check(String typed) {
        if (typed == null || keywords.length == 0) {
            return false;
        }
        String lower = typed.toLowerCase();
        for (int i = 0; i < keywords.length; i++) {
            if (!lower.contains(keywords[i])) {
                return false;
            }
        }
        return true;
    }
}

class Judge {
    Checker[] checkers;
    int[] weights;

    Judge(Checker[] checkers, int[] weights) {
        this.checkers = checkers;
        this.weights = weights;
    }

    String grade(String[] answers) {
        int n = (checkers == null || weights == null) ? 0 : Math.min(checkers.length, weights.length);
        int validCount = 0;
        int correctCount = 0;
        int total = 0;
        int earned = 0;
        for (int i = 0; i < n; i++) {
            if (checkers[i] == null || weights[i] <= 0) {
                continue;
            }
            validCount++;
            total += weights[i];
            String ans = (answers != null && i < answers.length) ? answers[i] : null;
            if (checkers[i].check(ans)) {
                correctCount++;
                earned += weights[i];
            }
        }
        if (validCount == 0) {
            return "無有效題目";
        }
        int pct = (earned * 100) / total;
        return "得分 " + earned + "/" + total + " (" + pct + "%)，答對 " + correctCount + "/" + validCount + " 題";
    }

    static String checkOne(Checker checker, String typed) {
        return (checker != null && checker.check(typed)) ? "答對了" : "再試一次";
    }
}
