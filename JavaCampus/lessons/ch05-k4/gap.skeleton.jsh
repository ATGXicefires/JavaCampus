static int max(int[] a) {
    int best = a[0];
    for (int i = 1; i < a.length; i++) {
        if (a[i] > best) {
            best = a[i];
        }
    }
    return best;
}

static int min(int[] a) {
    int best = a[0];
    for (int i = 1; i < a.length; i++) {
        if (a[i] < best) {
            best = a[i];
        }
    }
    return best;
}

System.out.println("最高和最低差了 " + (max(myScores) ____ min(myScores)) + " 分");
