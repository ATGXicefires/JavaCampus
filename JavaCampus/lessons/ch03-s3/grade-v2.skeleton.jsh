int finalScore = -1;
if (score < 0 || score > 100 || bonus < 0) {
    System.out.println("分數無效");
} else {
    finalScore = score + bonus;
    if (finalScore ____ 100) {
        finalScore = 100;
    }
    if (finalScore >= ____) {
        System.out.println(finalScore + " 分是 A");
    } else if (finalScore >= 80) {
        System.out.println(finalScore + " 分是 B");
    } else if (finalScore >= 70) {
        System.out.println(finalScore + " 分是 C");
    } else if (finalScore >= 60) {
        System.out.println(finalScore + " 分是 D");
    } ____ {
        System.out.println(finalScore + " 分是 F");
    }
}
