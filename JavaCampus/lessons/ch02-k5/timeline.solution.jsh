int seconds = totalMinutes * 60;
int perSlide = seconds / slides;
int extraSec = seconds % slides;
for (int i = 1; i <= slides; i++) {
    int start = (i - 1) * perSlide;
    int min = start / 60;
    int sec = start % 60;
    if (sec < 10) {
        System.out.println("第 " + i + " 張 " + min + ":0" + sec);
    } else {
        System.out.println("第 " + i + " 張 " + min + ":" + sec);
    }
}
if (extraSec > 0) {
    System.out.println("預留緩衝：" + extraSec + " 秒");
} else {
    System.out.println("時間剛剛好");
}
