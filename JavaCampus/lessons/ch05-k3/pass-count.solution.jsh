static double average(int[] a) {
    int sum = 0;
    for (int i = 0; i < a.length; i++) {
        sum += a[i];
    }
    return (double) sum / a.length;
}

static int passCount(int[] a) {
    int count = 0;
    for (int i = 0; i < a.length; i++) {
        if (a[i] >= 60) {
            count++;
        }
    }
    return count;
}
