int endSum = 0;
int endDiff = 0;
if (scores.length == ____) {
    System.out.println("空陣列");
} else if (scores.length == 1) {
    endSum = scores[0];
    endDiff = 0;
    System.out.println("首：" + scores[0] + " 尾：" + scores[0] + " 差：" + endDiff);
} else {
    int first = scores[____];
    int last = scores[scores.length ____];
    endSum = first + last;
    endDiff = last - first;
    System.out.println("首：" + first + " 尾：" + last + " 差：" + endDiff);
}
