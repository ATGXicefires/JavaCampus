static boolean showBadge(int done, int total, int score) {
    return done == total && score >= 60;
}

static boolean showHint(boolean triedTwice, boolean passed) {
    return triedTwice && !passed;
}
