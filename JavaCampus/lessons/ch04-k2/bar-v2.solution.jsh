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
