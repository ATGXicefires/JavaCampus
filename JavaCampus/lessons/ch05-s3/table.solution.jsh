for (int i = 1; i <= n; i++) {
    String line = "";
    for (int j = 1; j <= n; j++) {
        line += i + "x" + j + "=" + i * j + " ";
    }
    System.out.println(line);
}
