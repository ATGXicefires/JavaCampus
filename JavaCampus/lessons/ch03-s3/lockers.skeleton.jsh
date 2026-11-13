int max = 0;
int maxIndex = -1;
if (scores.length ____ 0) {
    System.out.println("置物櫃是空的");
} else {
    max = scores[0];
    maxIndex = 0;
    for (int i = 0; i < ____; i++) {
        System.out.println("第 " + i + " 格：" + scores[i]);
        if (scores[i] ____ max) {
            max = scores[i];
            maxIndex = i;
        }
    }
    System.out.println("最後一格是第 " + (scores.length ____) + " 格");
    System.out.println("最高分在第 " + maxIndex + " 格：" + max + " 分");
}
