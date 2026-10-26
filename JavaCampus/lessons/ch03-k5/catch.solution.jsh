try {
    int score = Integer.parseInt(input);
    System.out.println("收到：" + score);
} catch (NumberFormatException e) {
    System.out.println("要輸入數字喔，例如 80");
}
