abstract class ProgressScreen {
    String owner;

    ProgressScreen(String owner) {
        this.owner = (owner == null || owner.trim().isEmpty()) ? "學員" : owner.trim();
    }

    final int safePercent() {
        int pct = completionPercent();
        if (pct < 0) {
            return 0;
        }
        if (pct > 100) {
            return 100;
        }
        return pct;
    }

    final String export() {
        String rawTitle = title();
        String safeTitle = (rawTitle == null || rawTitle.trim().isEmpty()) ? "報表" : rawTitle.trim();
        String out = "[學習進度] " + owner + " - " + safeTitle;
        String[] rows = details();
        int valid = 0;
        if (rows != null) {
            for (int i = 0; i < rows.length; i++) {
                if (rows[i] != null && !rows[i].trim().isEmpty()) {
                    out += "\n  - " + rows[i].trim();
                    valid++;
                }
            }
        }
        if (valid == 0) {
            out += "\n  - (尚無紀錄)";
        }
        out += "\n完成度：" + safePercent() + "%";
        return out;
    }

    abstract String title();

    abstract String[] details();

    abstract int completionPercent();
}

class LessonProgress extends ProgressScreen {
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
        return total <= 0 ? 0 : (done * 100) / total;
    }
}

class QuizProgress extends ProgressScreen {
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
        return attempts <= 0 ? 0 : (correct * 100) / attempts;
    }
}

class StickerProgress extends ProgressScreen {
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

class StreakProgress extends ProgressScreen {
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
        if (days <= 0) {
            return new String[0];
        }
        return new String[]{"連續 " + days + " 天", "目標 " + Math.max(0, goalDays) + " 天"};
    }

    int completionPercent() {
        return goalDays <= 0 ? 0 : (days * 100) / goalDays;
    }
}

static int averageCompletion(ProgressScreen[] screens) {
    if (screens == null) {
        return 0;
    }
    int sum = 0;
    int count = 0;
    for (int i = 0; i < screens.length; i++) {
        if (screens[i] != null) {
            sum += screens[i].safePercent();
            count++;
        }
    }
    return count == 0 ? 0 : sum / count;
}
