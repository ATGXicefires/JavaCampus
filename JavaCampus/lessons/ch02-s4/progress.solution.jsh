for (int i = 1; i <= weeks; i++) {
    int percent = i * 100 / weeks;
    if (i < 10) {
        System.out.println("W0" + i + " " + percent + "%");
    } else {
        System.out.println("W" + i + " " + percent + "%");
    }
}
