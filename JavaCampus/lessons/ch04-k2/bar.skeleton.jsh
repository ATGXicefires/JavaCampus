static String bar(int done, int total, int width) {
    if (width <= 0) {
        return "";
    }
    int filled;
    if (total <= 0 || done <= 0) {
        filled = ____;
    } else if (done >= total) {
        filled = ____;
    } else {
        filled = ____;
    }
    return "█".repeat(filled) + "░".repeat(width - filled);
}
