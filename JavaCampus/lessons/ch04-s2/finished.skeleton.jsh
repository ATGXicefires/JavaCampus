// 她寫好的方法
static String bar(int done, int total) {
    return "■".repeat(done) + "□".repeat(total - done);
}

static void say(String who, String line) {
    System.out.println(who + "：「" + line + "」");
}

// 每一課印一行：課名、進度條、完成數/總數
for (int i = 0; i < units.length; i++) {
    System.out.println(units[i] + " " + bar(done[i], total[i]) + " " + done[i] + "/" + total[i]);
    if (done[i] == ____) {
        say("滷肉", "____");
    }
}
