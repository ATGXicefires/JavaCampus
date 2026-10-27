class ParentsPlan {
    String name;

    ParentsPlan(String name) {
        this.name = name;
    }

    String future() {
        return "學一技之長，找一份穩定的工作";
    }

    String hobby() {
        return "畫畫當興趣就好";
    }
}

class MyPlan extends ParentsPlan {
    // 寫建構子 MyPlan(String name)，用 super(name) 把名字交給父類別

    String future() {
        return super.future() + "，而且是我喜歡的那一種";
    }

    String hobby() {
        return "畫畫不只是興趣";
    }
}
