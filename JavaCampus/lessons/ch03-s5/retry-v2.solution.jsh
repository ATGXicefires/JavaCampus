int strong = 0;
int pass = 0;
int retry = 0;
int invalid = 0;
int validSum = 0;
int validCount = 0;
double validAvg = 0.0;
for (int i = 0; i < scores.length; i++) {
    int s = scores[i];
    if (s < 0 || s > 100) {
        invalid++;
    } else {
        validSum += s;
        validCount++;
        if (s >= 90) {
            strong++;
        } else if (s >= 60) {
            pass++;
        } else {
            retry++;
        }
    }
}
if (validCount > 0) {
    validAvg = (double) validSum / validCount;
}
System.out.println("高標：" + strong + " 及格：" + pass + " 重試：" + retry + " 無效：" + invalid + " 平均：" + validAvg);
