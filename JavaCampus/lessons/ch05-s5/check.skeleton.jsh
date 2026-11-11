// 她寫的方法（都是對的）
static boolean isPass(int score) {
    return score >= 60;
}

static int max(int[] a) {
    int best = a[0];
    for (int i = 1; i < a.length; i++) {
        if (a[i] > best) {
            best = a[i];
        }
    }
    return best;
}

static boolean showBadge(int done, int total, int score) {
    return done == total && score >= 60;
}

static String check(boolean actual, boolean expected) {
    if (actual ____ expected) {
        return "PASS";
    }
    return "____";
}
