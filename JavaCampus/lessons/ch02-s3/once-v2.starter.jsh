// 修好迴圈邊界（從 1 到 times），最後一遍印「第 X 遍：最後一遍！」
for (int i = 0; i < times; i++) {
    int remain = times - i;
    System.out.println("第 " + i + " 遍：剩 " + remain + " 遍");
}
