int validCount = 0;
int failCount = 0;
int validSum = 0;
double avg = 0.0;
for (int i = 0; i < inputs.length; i++) {
    try {
        int s = Integer.parseInt(inputs[i]);
        if (s < 0 || s > 100) {
            failCount++;
        } else {
            validSum += s;
            validCount++;
        }
    } catch (NumberFormatException e) {
        failCount++;
    }
}
if (validCount > 0) {
    avg = (double) validSum / validCount;
}
System.out.println("合法：" + validCount + " 筆，失敗：" + failCount + " 筆");
System.out.println("平均：" + avg);
