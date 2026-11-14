static String hint(String answer) {
    if (answer == null) {
        return "";
    }
    String clean = answer.____();
    if (clean.isEmpty()) {
        return "";
    }
    String out = clean.substring(0, 1);
    for (int i = 1; i < clean.length(); i++) {
        char ch = clean.charAt(i);
        if (ch == ' ' || ch == '　') {
            out += ____;
        } else {
            out += ____;
        }
    }
    return out;
}
