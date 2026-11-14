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

static String progress(String title, int done, int total, int width) {
    String clean = title.trim();
    if (clean.isEmpty()) {
        clean = "未命名";
    }
    int pct;
    if (total <= 0 || done <= 0) {
        pct = ____;
    } else if (done >= total) {
        pct = ____;
    } else {
        pct = ____;
    }
    return clean + " " + ____(done, total, width) + " " + pct + "%";
}
