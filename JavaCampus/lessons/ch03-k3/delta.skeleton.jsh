int maxGain = 0;
for (int i = 0; i < ____; i++) {
    int diff = scores[i + 1] - scores[i];
    if (diff ____ maxGain) {
        maxGain = diff;
    }
    if (diff > 0) {
        System.out.println("第 " + (i + 1) + " -> " + (i + 2) + " 次：+" + diff);
    } else {
        System.out.println("第 " + (i + 1) + " -> " + (i + 2) + " 次：" + diff);
    }
}
System.out.println("最大進步：" + maxGain);
