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
