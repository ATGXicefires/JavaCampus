// 成績頁：合規平均、無效排除等第、安全相鄰進步次數
int validSum = 0;
int validCount = 0;
for (int i = 0; i < scores.length; i++) {
    int s = scores[i];
    if (s >= 0 && s <= 100) {
        validSum += s;
        validCount++;
    }
}
double avg = 0.0;
if (validCount > 0) {
    avg = (double) validSum / validCount;
}
System.out.println("平均：" + avg);
for (int i = 0; i < scores.length; i++) {
    int s = scores[i];
    if (s < 0 || s > 100) {
        System.out.println(s + " 無效");
    } else if (s >= 90) {
        System.out.println(s + " A");
    } else if (s >= 80) {
        System.out.println(s + " B");
    } else if (s >= 70) {
        System.out.println(s + " C");
    } else if (s >= 60) {
        System.out.println(s + " D");
    } else {
        System.out.println(s + " F");
    }
}
int riseCount = 0;
for (int i = 0; i < scores.length - 1; i++) {
    int cur = scores[i];
    int next = scores[i + 1];
    if (cur >= 0 && cur <= 100 && next >= 0 && next <= 100 && next > cur) {
        riseCount++;
    }
}
System.out.println("連續進步次數：" + riseCount);
