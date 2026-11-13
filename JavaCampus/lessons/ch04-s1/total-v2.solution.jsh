static int total(int[] counts) {
    int sum = 0;
    for (int i = 0; i < counts.length; i++) {
        if (counts[i] > 0) {
            sum += counts[i];
        }
    }
    return sum;
}
