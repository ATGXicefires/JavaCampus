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

static int min(int[] a) {
    if (a == null || a.length == 0) {
        return Integer.MAX_VALUE;
    }
    int best = a[0];
    for (int i = 1; i < a.length; i++) {
        if (a[i] < best) {
            best = a[i];
        }
    }
    return best;
}
