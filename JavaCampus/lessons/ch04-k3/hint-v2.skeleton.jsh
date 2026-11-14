static String hint(String answer) {
    if (answer == null) {
        return "";
    }
    String clean = answer.____();
    if (clean.isEmpty()) {
        return "";
    }
    if (clean.length() == 1) {
        return clean;
    }
    return clean.substring(0, ____) + "_".repeat(____);
}
