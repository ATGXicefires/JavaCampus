// 她寫的：從第 1 天到第 days 天，奇數天做 2 題、偶數天做 3 題，印出每天累計題數
// 少了最後一天，且沒處理 days <= 0 時印出 尚未排程
int done = 0;
for (int i = 1; i < days; i++) {
    if (i % 2 == 1) {
        done += 2;
    } else {
        done += 3;
    }
    System.out.println("第 " + i + " 天：累計 " + done + " 題");
}
