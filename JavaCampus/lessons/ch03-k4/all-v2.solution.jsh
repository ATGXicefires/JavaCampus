int countA = 0;
int countB = 0;
int countC = 0;
int countD = 0;
int countF = 0;
int invalidCount = 0;
int validSum = 0;
int validCount = 0;
double validAvg = 0.0;
for (int i = 0; i < scores.length; i++) {
    int s = scores[i];
    if (s < 0 || s > 100) {
        System.out.println(s + " 無效");
        invalidCount++;
    } else {
        validSum += s;
        validCount++;
        if (s >= 90) {
            System.out.println(s + " A");
            countA++;
        } else if (s >= 80) {
            System.out.println(s + " B");
            countB++;
        } else if (s >= 70) {
            System.out.println(s + " C");
            countC++;
        } else if (s >= 60) {
            System.out.println(s + " D");
            countD++;
        } else {
            System.out.println(s + " F");
            countF++;
        }
    }
}
if (validCount > 0) {
    validAvg = (double) validSum / validCount;
}
System.out.println("統計：A=" + countA + " B=" + countB + " C=" + countC + " D=" + countD + " F=" + countF + " 無效=" + invalidCount);
System.out.println("有效平均：" + validAvg);
