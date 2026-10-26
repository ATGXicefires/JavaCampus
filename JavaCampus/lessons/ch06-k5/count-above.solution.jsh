static int countAbove(int[] a, int line) {
    int count = 0;
    for (int i = 0; i < a.length; i++) {
        if (a[i] >= line) {
            count++;
        }
    }
    return count;
}
