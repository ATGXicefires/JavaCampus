static String bar(int done, int total) {
    return "■".repeat(done) + "□".repeat(total - done);
}
