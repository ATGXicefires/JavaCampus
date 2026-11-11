for (int i = 0; i < scores.length; i++) {
    int s = scores[i];
    if (s >= 90) {
        System.out.println(s + " 分 滷肉：「太強了！」");
    } else if (s >= 60) {
        System.out.println(s + " 分 滷肉：「不錯喔！」");
    } else {
        System.out.println(s + " 分 滷肉：「沒關係，再跑一次！」");
    }
}
