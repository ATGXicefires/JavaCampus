int done = 0;
if (days ____ 0) {
    System.out.println("尚未排程");
} else {
    for (int i = 1; i ____ days; i++) {
        if (i % 2 == 1) {
            done += 2;
        } else {
            done += 3;
        }
        System.out.println("第 " + i + " 天：累計 " + done + " 題");
    }
}
