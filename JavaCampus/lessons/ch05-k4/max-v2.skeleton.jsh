static int max(int[] a) {
    if (a == null || a.length == 0) {
        return Integer.MIN_VALUE;
    }
    int best = ____;
    for (int i = 1; i < a.length; i++) {
        if (a[i] ____ best) {
            best = a[i];
        }
    }
    return best;
}
