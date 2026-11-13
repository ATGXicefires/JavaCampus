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

if (n > 0) {
    System.out.println(multRow(2, n));
}
