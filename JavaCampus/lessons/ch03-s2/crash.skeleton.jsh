// 成績頁：加總、平均、每個分數的等第
int sum = 0;
for (int i = 0; i ____ scores.length; i++) {
    sum += scores[i];
}
double avg = sum / scores.length;
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
