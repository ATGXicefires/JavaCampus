int seconds = totalMinutes * 60;
int perSlide = seconds / slides;
// 修好兩處整數截斷錯誤
double exact = (double) (seconds / slides);
double usedRatio = seconds / 600 * 100;
