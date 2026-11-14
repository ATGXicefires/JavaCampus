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
        if (plans == null) {
            return 0;
        }
        int total = 0;
        for (Plan p : plans) {
            if (p != null) {
                total += p.totalHours();
            }
        }
        return total;
    }
}

class MyPlan extends Plan {
    String artNote;
    int extraHours;

    MyPlan(String owner, int studyHours, String artNote, int extraHours) {
        String cleaned = owner == null ? "" : owner.trim();
        String fixedOwner = (!cleaned.isEmpty() && !cleaned.startsWith("學妹-")) ? "學妹-" + cleaned : cleaned;
        super(fixedOwner, studyHours);
        this.artNote = (artNote == null || artNote.isBlank()) ? "畫畫" : artNote.trim();
        if (extraHours < 0) {
            this.extraHours = 0;
        } else if (extraHours > 20) {
            this.extraHours = 20;
        } else {
            this.extraHours = extraHours;
        }
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
