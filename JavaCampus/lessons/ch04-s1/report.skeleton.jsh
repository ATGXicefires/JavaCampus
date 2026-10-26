static int total(int[] counts) {
    int sum = 0;
    for (int i = 0; i < counts.length; i++) {
        sum += counts[i];
    }
    return sum;
}

static int percent(int part, int whole) {
    if (whole == 0) {
        return 0;
    }
    return part * 100 / whole;
}

static void report(int part, int whole) {
    System.out.println("完成 " + ____(part, whole) + "%");
}

report(1, 4);
report(3, 4);
