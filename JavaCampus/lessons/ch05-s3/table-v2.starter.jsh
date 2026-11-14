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

// 請實作 static void printTable(int rows, int cols)
// 若 rows <= 0 或 cols <= 0 什麼都不印；否則印出 1..rows 列的乘法表，每列乘到 cols
// 最後呼叫 printTable(n, n);
