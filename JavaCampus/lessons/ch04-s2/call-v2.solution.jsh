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

static int calcPercent(int done, int total) {
    if (total <= 0 || done <= 0) {
        return 0;
    }
    if (done >= total) {
        return 100;
    }
    return done * 100 / total;
}

for (int i = 0; i < units.length; i++) {
    int p = calcPercent(done[i], total[i]);
    System.out.println(units[i] + " " + bar(done[i], total[i], 6) + " " + done[i] + "/" + total[i] + " (" + p + "%)");
}
