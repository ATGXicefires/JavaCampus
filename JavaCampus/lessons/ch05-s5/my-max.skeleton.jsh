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
    if (actual == expected) {
        return "PASS";
    }
    return "FAIL";
}

int[] tests = {59, 60, 61, 0, 100};
boolean[] expected = {false, true, true, false, true};
for (int i = 0; i < tests.length; i++) {
    System.out.println("isPass(" + tests[i] + ") " + check(isPass(tests[i]), expected[i]));
}

static int myMax(int[] a) {
    int best = ____;
    for (int i = 1; i < a.length; i++) {
        if (a[i] > best) {
            best = a[i];
        }
    }
    return best;
}

int[] t1 = {-3, -7};
int[] t2 = {5};
int[] t3 = {62, 41, 34};
System.out.println("第 1 組 " + check(max(t1) == myMax(t1), true));
System.out.println("第 2 組 " + check(max(t2) == myMax(t2), true));
System.out.println("第 3 組 " + check(max(t3) == ____, true));
