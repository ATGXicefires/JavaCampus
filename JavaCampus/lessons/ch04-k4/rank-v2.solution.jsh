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
    int sum = total(validCounts);
    int maxIdx = 0;
    int minIdx = 0;
    for (int i = 1; i < n; i++) {
        if (counts[i] > counts[maxIdx]) {
            maxIdx = i;
        }
        if (counts[i] < counts[minIdx]) {
            minIdx = i;
        }
    }
    return "總星數：" + sum + "，最高：" + names[maxIdx] + "(" + counts[maxIdx] + "★)，最低："
            + names[minIdx] + "(" + counts[minIdx] + "★)";
}

String reportSummary = bestAndWorst(flavors, stars);

static void printFeatured(String[] names, int[] counts, int minStars) {
    int n = Math.min(names.length, counts.length);
    int[] validCounts = new int[n];
    for (int i = 0; i < n; i++) {
        validCounts[i] = counts[i];
    }
    int sum = total(validCounts);
    int shown = 0;
    for (int i = 0; i < n; i++) {
        if (counts[i] >= minStars) {
            int s = Math.max(0, counts[i]);
            System.out.println(names[i] + " " + "★".repeat(s) + " " + percent(s, sum) + "%");
            shown++;
        }
    }
    if (shown == 0) {
        System.out.println("沒有符合門檻的口味");
    }
}

printFeatured(flavors, stars, 4);
