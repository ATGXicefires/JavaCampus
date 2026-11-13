// 成績頁：加總和平均（目前有越界與空陣列除以零的問題）
int sum = 0;
for (int i = 0; i <= scores.length; i++) {
    sum += scores[i];
}
double avg = sum / scores.length;
System.out.println("平均：" + avg);
