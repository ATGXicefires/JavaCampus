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

int[] passTests = {59, 60, 61, 0, 100, -1, 101};
boolean[] passExpected = {false, true, true, ____, true, false, ____};
for (int i = 0; i < passTests.length; i++) {
    System.out.println("isPass(" + passTests[i] + ") " + check(isPass(passTests[i]), passExpected[i]));
}

int[] d = {5, 5, 4, 0};
int[] tot = {5, 5, 5, 0};
int[] sc = {60, 59, 100, 100};
boolean[] badgeExpected = {true, false, false, ____};
for (int i = 0; i < d.length; i++) {
    System.out.println("showBadge(" + d[i] + "," + tot[i] + "," + sc[i] + ") " + check(showBadge(d[i], tot[i], sc[i]), badgeExpected[i]));
}
