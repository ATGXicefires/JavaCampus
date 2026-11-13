static int toScore(String s) {
    if (s == null) {
        return -1;
    }
    try {
        int score = Integer.parseInt(s.____());
        if (score < 0 || score ____ 100) {
            return -1;
        }
        return score;
    } catch (____ e) {
        return -1;
    }
}
