// 成績頁：加總和平均
int sum = 0;
for (int i = 0; i ____ scores.length; i++) {
    sum += scores[i];
}
double avg = 0.0;
if (scores.length ____ 0) {
    avg = ____ sum / scores.length;
}
System.out.println("平均：" + avg);
