int validCount = 0;
int invalidCount = 0;
int validSum = 0;
int safeAvg = 0;
for (int i = 0; i < inputs.length; i++) {
    try {
        int s = Integer.parseInt(inputs[i]);
        if (s < 0 || s > 100) {
            invalidCount++;
        } else {
            validSum += s;
            validCount++;
        }
    } catch (NumberFormatException e) {
        invalidCount++;
    }
}
if (validCount > 0) {
    safeAvg = validSum / validCount;
}
System.out.println("合法：" + validCount + " 筆，失敗：" + invalidCount + " 筆，平均：" + safeAvg);
