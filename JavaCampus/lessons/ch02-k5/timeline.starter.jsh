int seconds = totalMinutes * 60;
int perSlide = seconds / slides;
int extraSec = seconds % slides;
// 用 for 印出第 1 到 slides 張的開始時間（M:SS，秒數小於 10 補 0），最後印緩衝時間
