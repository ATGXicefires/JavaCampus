double avg = 0.0;
int aboveCount = 0;
int max = 0;
if (scores.length ____ 0) {
    int sum = 0;
    max = scores[____];
    for (int i = 0; i < scores.length; i++) {
        sum += scores[i];
        if (scores[i] > max) {
            max = scores[i];
        }
    }
    avg = ____ sum / scores.length;
    for (int i = 0; i < scores.length; i++) {
        if (scores[i] ____ avg) {
            aboveCount++;
        }
    }
}
System.out.println("平均：" + avg);
System.out.println("高於平均：" + aboveCount + " 個（最高 " + max + " 分）");
