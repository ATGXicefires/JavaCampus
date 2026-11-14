class Heart {
    private String real = "想見學長";
    private int totalSteps;
    private int progress;

    Heart() {
        this(5);
    }

    Heart(int totalSteps) {
        if (totalSteps <= 0) {
            this.totalSteps = 1;
        } else {
            this.totalSteps = totalSteps;
        }
    }

    void advance(int delta) {
        progress += delta;
        if (progress < 0) {
            progress = 0;
        } else if (progress > totalSteps) {
            progress = totalSteps;
        }
    }

    int getProgress() {
        return progress;
    }

    int getTotalSteps() {
        return totalSteps;
    }

    String status() {
        if (progress == totalSteps) {
            return "其實很想你";
        }
        return "沒事";
    }

    String whatAreYouDoing() {
        return "在幹嘛";
    }
}
