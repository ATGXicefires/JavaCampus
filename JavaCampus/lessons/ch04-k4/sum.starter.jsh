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

// 請實作 static String bestAndWorst(String[] names, int[] counts)
// 並在最後宣告 String reportSummary = bestAndWorst(flavors, stars);
