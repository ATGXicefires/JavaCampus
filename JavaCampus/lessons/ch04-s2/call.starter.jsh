// 她寫好的方法，不要改
static String bar(int done, int total, int width) {
    if (width <= 0) {
        return "";
    }
    int filled;
    if (total <= 0 || done <= 0) {
        filled = 0;
    } else if (done >= total) {
        filled = width;
    } else {
        filled = done * width / total;
    }
    return "█".repeat(filled) + "░".repeat(width - filled);
}

static void say(String who, String line) {
    System.out.println(who + "：「" + line + "」");
}

// TODO: 實作 calcPercent(int done, int total) 並修正底下草稿的簽章順序與百分比計算
for (int i = 0; i < units.length; i++) {
    int p = done[i] / total[i] * 100;
    System.out.println(units[i] + " " + bar(total[i], done[i], 6) + " " + done[i] + "/" + total[i] + " (" + p + "%)");
}
