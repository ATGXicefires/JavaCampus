for (int i = 0; i < scores.length; i++) {
    int s = scores[i];
    if (s >= 90) {
        System.out.println(s + " A");
    } else if (s >= 80) {
        System.out.println(s + " B");
    } else if (s >= 70) {
        System.out.println(s + " C");
    } else if (s >= 60) {
        System.out.println(s + " D");
    } else {
        System.out.println(s + " F");
    }
}
