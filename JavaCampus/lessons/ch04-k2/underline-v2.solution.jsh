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
        pct = 0;
    } else if (done >= total) {
        pct = 100;
    } else {
        pct = done * 100 / total;
    }
    return clean + " " + bar(done, total, width) + " " + pct + "%";
}

static String underline(String title, int minWidth) {
    String clean = title.trim();
    if (clean.isEmpty()) {
        clean = "未命名";
    }
    int len = clean.length();
    if (minWidth > len) {
        len = minWidth;
    }
    return clean + "\n" + "=".repeat(len);
}
