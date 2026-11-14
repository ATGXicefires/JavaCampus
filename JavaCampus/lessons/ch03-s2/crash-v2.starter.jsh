// 成績頁：加總、平均、每個分數的等第（目前有越界、未排除無效分數與除零問題）
int validSum = 0;
int validCount = 0;
for (int i = 0; i <= scores.length; i++) {
    validSum += scores[i];
}
double avg = validSum / scores.length;
System.out.println("平均：" + avg);
for (int i = 0; i < scores.length; i++) {
    int s = scores[i];
    if (s >= 60) {
        System.out.println(s + " D");
    } else if (s >= 70) {
        System.out.println(s + " C");
    } else if (s >= 80) {
        System.out.println(s + " B");
    } else {
        System.out.println(s + " F");
    }
}
