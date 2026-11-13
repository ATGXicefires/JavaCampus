// 成績頁：先修好越界、只計入 0～100 有效分數並安全算出平均 avg
int validSum = 0;
int validCount = 0;
for (int i = 0; i ____ scores.length; i++) {
    int s = scores[i];
    if (s ____ 0 && s ____ 100) {
        validSum += s;
        validCount++;
    }
}
double avg = 0.0;
if (validCount ____ 0) {
    avg = ____ validSum / validCount;
}
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
