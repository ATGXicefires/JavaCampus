static String normalize(String text) {
    if (text == null) {
        return "";
    }
    return text.replace('　', ' ').trim().replaceAll("____", " ").toLowerCase();
}

static boolean isCorrect(String typed, String answer) {
    String t = normalize(typed);
    String a = normalize(answer);
    if (t.isEmpty() || a.isEmpty()) {
        return ____;
    }
    return t.____(a);
}
