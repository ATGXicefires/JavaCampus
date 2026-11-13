static String hint(String answer) {
    if (answer == null) {
        return "";
    }
    String clean = answer.strip();
    if (clean.isEmpty()) {
        return "";
    }
    if (clean.length() == 1) {
        return clean;
    }
    return clean.substring(0, 1) + "_".repeat(clean.length() - 1);
}

static boolean isCorrect(String typed, String answer) {
    if (typed == null || answer == null) {
        return false;
    }
    String t = typed.____();
    String a = answer.____();
    if (t.isEmpty() || a.isEmpty()) {
        return ____;
    }
    return t.____(a);
}
