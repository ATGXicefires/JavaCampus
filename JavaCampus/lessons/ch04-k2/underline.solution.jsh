static String bar(int done, int total) {
    return "■".repeat(done) + "□".repeat(total - done);
}

static String progress(int done, int total) {
    return bar(done, total) + " " + done + "/" + total;
}

static String underline(String title) {
    return "=".repeat(title.length());
}
