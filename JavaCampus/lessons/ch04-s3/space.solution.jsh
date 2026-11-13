static String normalize(String text) {
    if (text == null) {
        return "";
    }
    return text.strip().toLowerCase();
}

static boolean isCorrect(String typed, String answer) {
    if (answer == null) {
        return false;
    }
    return normalize(typed).equals(answer);
}
