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
    MyPlan(String name) {
        ____(name);
    }

    String future() {
        return super.future() + "，而且是我喜歡的那一種";
    }

    String hobby() {
        return "畫畫不只是興趣";
    }
}
