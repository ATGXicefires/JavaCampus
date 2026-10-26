static String bar(int done, int total) {
    return "■".repeat(done) + "□".repeat(total - done);
}

static String progress(int done, int total) {
    return ____(done, total) + " " + done + "/" + total;
}
