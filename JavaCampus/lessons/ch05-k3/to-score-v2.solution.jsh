static int toScore(String s) {
    if (s == null) {
        return -1;
    }
    try {
        int score = Integer.parseInt(s.trim());
        if (score < 0 || score > 100) {
            return -1;
        }
        return score;
    } catch (NumberFormatException e) {
        return -1;
    }
}
