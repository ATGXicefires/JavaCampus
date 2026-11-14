static String normalize(String text) {
    if (text == null) {
        return "";
    }
    return text.____().toLowerCase();
}

static boolean isCorrect(String typed, String answer) {
    if (answer == null) {
        return false;
    }
    return normalize(typed).____(answer);
}
