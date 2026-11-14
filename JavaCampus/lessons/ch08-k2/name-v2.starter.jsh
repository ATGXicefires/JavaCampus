class Plan {
    String owner;
    int studyHours;

    Plan(String owner, int studyHours) {
        this.owner = (owner == null || owner.isBlank()) ? "同學" : owner.trim();
        if (studyHours < 0) {
            this.studyHours = 0;
        } else if (studyHours > 60) {
            this.studyHours = 60;
        } else {
            this.studyHours = studyHours;
        }
    }

    String future() {
        return "學一技之長，找一份穩定的工作";
    }

    int totalHours() {
        return studyHours;
    }

    String describe() {
        return owner + "：" + future() + "（每週 " + totalHours() + " 小時）";
    }

    static int sumHours(Plan[] plans) {
        // 請實作多型加總所有非 null 計畫的 totalHours()
        return 0;
    }
}

class MyPlan extends Plan {
    String artNote;
    int extraHours;

    // 寫建構子 MyPlan(String owner, int studyHours, String artNote, int extraHours)
    // 處理 owner 前綴「學妹-」與各項防呆後透過 super(...) 交給父類別

    @Override
    String future() {
        return super.future() + "，而且加上" + artNote;
    }

    @Override
    int totalHours() {
        return super.totalHours() + extraHours;
    }
}
