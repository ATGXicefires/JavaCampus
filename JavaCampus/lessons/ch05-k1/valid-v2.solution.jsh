static boolean isValid(int score, int absent) {
    return score >= 0 && score <= 100 && absent >= 0 && absent <= 18;
}
