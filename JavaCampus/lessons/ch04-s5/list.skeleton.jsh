static String review(int sweetness, int star) {
    if (sweetness < 1 || sweetness > 5 || star < 1 || star > 5) {
        return "資料異常";
    }
    if (star >= 4 && sweetness <= 3) {
        return "招牌必點";
    } else if (sweetness >= 5) {
        return "太甜小心";
    } else if (star <= 2) {
        return "尚待改進";
    } else {
        return "表現普通";
    }
}

static void printReport(String[] names, int[] sweets, int[] starList) {
    int n = Math.min(names.length, Math.min(sweets.length, starList.length));
    if (n == 0) {
        System.out.println("沒有任何口味資料");
        return;
    }
    int sum = 0;
    int validCount = 0;
    int maxIdx = -1;
    int minIdx = -1;
    for (int i = 0; i < n; i++) {
        System.out.println(names[i] + "：甜度" + sweets[i] + "/星等" + starList[i] + " -> " + ____(sweets[i], starList[i]));
        if (starList[i] >= 1 && starList[i] <= 5) {
            sum += starList[i];
            validCount++;
            if (maxIdx == -1 || starList[i] > starList[maxIdx]) {
                maxIdx = ____;
            }
            if (minIdx == -1 || starList[i] < starList[minIdx]) {
                minIdx = ____;
            }
        }
    }
    if (validCount == 0) {
        System.out.println("平均星等：無有效評分");
        return;
    }
    double avg = Math.round(((double) sum / validCount) * 10) / 10.0;
    String tier;
    if (avg >= 4.0) {
        tier = "人氣攤位";
    } else if (avg >= 3.0) {
        tier = "穩定好評";
    } else {
        tier = "需要調整";
    }
    System.out.println("平均星等：" + avg + "（" + tier + "），最高：" + names[maxIdx] + "，最低：" + names[minIdx]);
}

printReport(flavors, sweet, stars);
