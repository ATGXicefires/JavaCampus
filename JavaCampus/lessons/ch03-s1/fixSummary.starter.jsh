// 她寫的：統計總分、平均，並數出剛好 60 分有幾科
int sum = 0;
int exactSixty = 0;
for (int i = 0; i < scores.length; i++) {
    sum += scores[i];
    if (scores[i] = 60) {
        exactSixty++;
    }
}
double avg = sum / scores.length；
System.out.println("平均：" + avg + "，剛好 60 分：" + exactSixty + " 科");
