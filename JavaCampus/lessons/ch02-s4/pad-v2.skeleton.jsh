for (int i = 1; i <= weeks; i++) {
    String label = "W" + i;
    if (____) {
        label = "W0" + i;
    }
    if (____) {
        System.out.println(label + " [發表]");
    } else if (i % 2 == 0) {
        System.out.println(label + " [檢討] (剩 " + (____) + " 週)");
    } else {
        System.out.println(label + " [開發] (剩 " + (____) + " 週)");
    }
}
