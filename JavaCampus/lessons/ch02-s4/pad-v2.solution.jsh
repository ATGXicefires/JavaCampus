for (int i = 1; i <= weeks; i++) {
    String label = "W" + i;
    if (i < 10) {
        label = "W0" + i;
    }
    if (i == weeks) {
        System.out.println(label + " [發表]");
    } else if (i % 2 == 0) {
        System.out.println(label + " [檢討] (剩 " + (weeks - i) + " 週)");
    } else {
        System.out.println(label + " [開發] (剩 " + (weeks - i) + " 週)");
    }
}
