static String multRow(int base, int limit) {
    if (limit <= 0) {
        return "";
    }
    String line = "";
    for (int j = 1; j <= limit; j++) {
        if (j > 1) {
            line += " ";
        }
        line += base + "x" + j + "=" + (base * j);
    }
    return line;
}

static void printTable(int rows, int cols) {
    if (rows <= 0 || cols <= 0) {
        return;
    }
    for (int i = 1; i <= rows; i++) {
        System.out.println(multRow(i, cols));
    }
}

printTable(n, n);
