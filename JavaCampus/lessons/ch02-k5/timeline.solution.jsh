int seconds = 5 * 60;
int perSlide = seconds / slides;
double exact = (double) seconds / slides;
for (int i = 1; i <= slides; i++) {
    System.out.println("第 " + i + " 張：" + perSlide * (i - 1) + " 秒");
}
