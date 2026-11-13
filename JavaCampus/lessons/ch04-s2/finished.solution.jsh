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

int completed = 0;
for (int i = 0; i < units.length; i++) {
    int p = calcPercent(done[i], total[i]);
    System.out.println(units[i] + " " + bar(done[i], total[i], 6) + " " + done[i] + "/" + total[i] + " (" + p + "%)");
    if (total[i] > 0 && done[i] >= total[i]) {
        say("滷肉", units[i] + " 完成！");
        completed++;
    }
}
if (units.length > 0 && completed == units.length) {
    say("滷肉", "全部完成了！");
} else {
    say("滷肉", "已完成 " + completed + "/" + units.length + " 課，繼續加油！");
}
