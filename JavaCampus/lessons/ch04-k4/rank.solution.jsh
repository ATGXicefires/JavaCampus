// 已經寫好的方法，直接呼叫就好，不用改
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

int sum = total(stars);
for (int i = 0; i < flavors.length; i++) {
    System.out.println(flavors[i] + " " + "★".repeat(stars[i]) + " " + percent(stars[i], sum) + "%");
}
