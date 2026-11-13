// 學長寫好的工具方法，照簽章呼叫就好，不要改
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

static String bestAndWorst(String[] names, int[] counts) {
    int n = Math.min(names.length, counts.length);
    if (n == 0) {
        return "無資料";
    }
    int[] validCounts = new int[n];
    for (int i = 0; i < n; i++) {
        validCounts[i] = counts[i];
    }
    int sum = ____(validCounts);
    int maxIdx = 0;
    int minIdx = 0;
    for (int i = 1; i < n; i++) {
        if (counts[i] > counts[maxIdx]) {
            maxIdx = ____;
        }
        if (counts[i] < counts[minIdx]) {
            minIdx = ____;
        }
    }
    return "總星數：" + sum + "，最高：" + names[maxIdx] + "(" + counts[maxIdx] + "★)，最低："
            + names[minIdx] + "(" + counts[minIdx] + "★)";
}

String reportSummary = bestAndWorst(flavors, stars);
