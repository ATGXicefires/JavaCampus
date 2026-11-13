class StreakProgress extends ____ {
    int days;
    int goalDays;

    StreakProgress(String owner, int days, int goalDays) {
        super(owner);
        this.days = days;
        this.goalDays = goalDays;
    }

    String title() {
        return "連續打卡";
    }

    String[] details() {
        ____
    }

    int completionPercent() {
        return ____;
    }
}

static int averageCompletion(ProgressScreen[] screens) {
    ____
}
