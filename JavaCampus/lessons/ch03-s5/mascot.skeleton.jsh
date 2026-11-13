for (int i = 0; i < scores.length; i++) {
    int s = scores[i];
    if (s < ____ || s > ____) {
        System.out.println(s + " 分 滷肉：「這個分數有問題！」");
    } else if (s >= ____) {
        System.out.println(s + " 分 滷肉：「太強了！」");
    } else if (s >= 70) {
        System.out.println(s + " 分 滷肉：「很穩喔！」");
    } else if (s >= 60) {
        System.out.println(s + " 分 滷肉：「低空飛過！」");
    } else {
        System.out.println(s + " 分 滷肉：「沒關係，再跑一次！」");
    }
}
