int sum = 0;
int exactSixty = 0;
for (int i = 0; i < scores.length; i++) {
    sum += scores[i];
    if (scores[i] == 60) {
        exactSixty++;
    }
}
double avg = 0.0;
if (scores.length > 0) {
    avg = (double) sum / scores.length;
}
System.out.println("平均：" + avg + "，剛好 60 分：" + exactSixty + " 科");
