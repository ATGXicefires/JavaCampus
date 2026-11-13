try {
    int score = Integer.parseInt(input);
    if (score < 0 || score > 100) {
        System.out.println("分數超出範圍");
    } else {
        int bonusScore = score + 5;
        if (bonusScore > 100) {
            bonusScore = 100;
        }
        System.out.println(score + " 分，加 5 分後是 " + bonusScore + " 分");
    }
} catch (NumberFormatException e) {
    System.out.println("無法轉成數字");
}
