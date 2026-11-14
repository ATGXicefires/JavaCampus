int score = -1;
try {
    int parsed = Integer.parseInt(input);
    if (parsed < 0 || parsed > 100) {
        System.out.println("超出範圍");
    } else {
        score = parsed;
        System.out.println("收到：" + score);
    }
} catch (NumberFormatException e) {
    System.out.println("格式錯誤");
}
