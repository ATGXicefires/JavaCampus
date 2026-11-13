abstract class ProgressScreen {
    String owner;

    ProgressScreen(String owner) {
        this.owner = (owner == null || owner.trim().isEmpty()) ? "學員" : owner.trim();
    }

    final int safePercent() {
        int pct = completionPercent();
        ____
    }

    final String export() {
        ____
    }

    abstract String title();

    abstract String[] details();

    abstract int completionPercent();
}

class LessonProgress extends ____ {
    int done;
    int total;

    LessonProgress(String owner, int done, int total) {
        super(owner);
        this.done = done;
        this.total = total;
    }

    String title() {
        return "課程單元";
    }

    String[] details() {
        return new String[]{"已完成 " + Math.max(0, done) + " 堂", "總計 " + Math.max(0, total) + " 堂"};
    }

    int completionPercent() {
        return ____;
    }
}

class QuizProgress extends ____ {
    int correct;
    int attempts;

    QuizProgress(String owner, int correct, int attempts) {
        super(owner);
        this.correct = correct;
        this.attempts = attempts;
    }

    String title() {
        return "測驗紀錄";
    }

    String[] details() {
        return new String[]{"答對 " + Math.max(0, correct) + " 題", "作答 " + Math.max(0, attempts) + " 次"};
    }

    int completionPercent() {
        return ____;
    }
}

class StickerProgress extends ____ {
    int collected;

    StickerProgress(String owner, int collected) {
        super(owner);
        this.collected = collected;
    }

    String title() {
        return "貼紙蒐集";
    }

    String[] details() {
        return new String[]{"已收集 " + Math.max(0, collected) + " 張貼紙"};
    }

    int completionPercent() {
        return collected * 10;
    }
}
