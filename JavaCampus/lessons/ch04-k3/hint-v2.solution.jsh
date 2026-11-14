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
