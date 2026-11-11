class ParentsPlan {
    String future() {
        return "學一技之長，找一份穩定的工作";
    }

    String hobby() {
        return "畫畫當興趣就好";
    }
}

class MyPlan extends ParentsPlan {
    String hobby() {
        return "畫畫不只是興趣";
    }

    // 覆寫 future()：用 super.future() 拿到爸媽原本那一句，後面接上「，而且是我喜歡的那一種」
}
