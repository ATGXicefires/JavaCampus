static boolean showBadge(int done, int total, int score) {
    return total > 0 && done == total && score >= 60 && score <= 100;
}

static boolean showHint(int tries, boolean passed, boolean practice) {
    return ____passed && (tries >= 2 ____ practice);
}
