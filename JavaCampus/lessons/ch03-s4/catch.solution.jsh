// 段考筆記：字串變數字
try {
    int score = Integer.parseInt(input);
    System.out.println(score + " 分，再加 5 分是 " + (score + 5) + " 分");
} catch (NumberFormatException e) {
    System.out.println("要輸入數字喔，例如 80");
}
