for (int i = 1; i <= weeks; i++) {
    String label = "W" + i;
    if (i < 10) {
        label = "W0" + i;
    }
    int percent = i * ____ / weeks;
    int filled = i * ____ / weeks;
    String bar = "";
    for (int b = 0; b < 10; b++) {
        if (____) {
            bar += "#";
        } else {
            bar += ".";
        }
    }
    System.out.println(label + " [" + bar + "] " + percent + "%");
}
