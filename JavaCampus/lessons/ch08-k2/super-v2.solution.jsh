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
}

class MyPlan extends Plan {
    String artNote;
    int extraHours;

    MyPlan(String owner, int studyHours, String artNote, int extraHours) {
        super(owner, studyHours);
        this.artNote = (artNote == null || artNote.isBlank()) ? "畫畫" : artNote.trim();
        this.extraHours = extraHours < 0 ? 0 : extraHours;
    }

    @Override
    String future() {
        return super.future() + "，而且加上" + artNote;
    }

    @Override
    int totalHours() {
        return super.totalHours() + extraHours;
    }
}
