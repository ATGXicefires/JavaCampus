for (int i = 1; i <= weeks; i++) {
    String label = "W" + i;
    if (i < 10) {
        label = "W0" + i;
    }
    int percent = i * 100 / weeks;
    int filled = i * 10 / weeks;
    String bar = "";
    for (int b = 0; b < 10; b++) {
        if (b < filled) {
            bar += "#";
        } else {
            bar += ".";
        }
    }
    System.out.println(label + " [" + bar + "] " + percent + "%");
}
