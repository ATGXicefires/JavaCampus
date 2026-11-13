for (int i = 1; i <= times; i++) {
    int remain = times - i;
    if (i == times) {
        System.out.println("第 " + i + " 遍：最後一遍！");
    } else {
        System.out.println("第 " + i + " 遍：剩 " + remain + " 遍");
    }
}
