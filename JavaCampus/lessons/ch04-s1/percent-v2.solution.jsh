static int total(int[] counts) {
    int sum = 0;
    for (int i = 0; i < counts.length; i++) {
        if (counts[i] > 0) {
            sum += counts[i];
        }
    }
    return sum;
}

static int percent(int part, int whole) {
    if (whole <= 0 || part <= 0) {
        return 0;
    }
    if (part >= whole) {
        return 100;
    }
    return part * 100 / whole;
}
