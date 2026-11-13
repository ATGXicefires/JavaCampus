// 她寫的方法（都是對的）
static boolean isPass(int score) {
    return score >= 60 && score <= 100;
}

static int max(int[] a) {
    if (a == null || a.length == 0) {
        return Integer.MIN_VALUE;
    }
    int best = a[0];
    for (int i = 1; i < a.length; i++) {
        if (a[i] > best) {
            best = a[i];
        }
    }
    return best;
}

static boolean showBadge(int done, int total, int score) {
    return total > 0 && done == total && score >= 60 && score <= 100;
}

static String check(boolean actual, boolean expected) {
    if (actual == expected) {
        return "PASS";
    }
    return "FAIL";
}

static String check(int actual, int expected) {
    if (actual == expected) {
        return "PASS";
    }
    return "FAIL";
}
