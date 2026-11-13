// 下面三個類別複製貼上了同樣的組裝邏輯，而且第三個已經悄悄寫錯了！
// 請抽出 abstract class ProgressScreen，讓三個類別改成 extends ProgressScreen

class LessonProgress {
    String owner;
    int done;
    int total;

    LessonProgress(String owner, int done, int total) {
        this.owner = (owner == null || owner.trim().isEmpty()) ? "學員" : owner.trim();
        this.done = done;
        this.total = total;
    }

    String export() {
        int pct = total <= 0 ? 0 : Math.max(0, Math.min(100, (done * 100) / total));
        return "[學習進度] " + owner + " - 課程單元\n  - 已完成 " + Math.max(0, done) + " 堂\n  - 總計 " + Math.max(0, total) + " 堂\n完成度：" + pct + "%";
    }
}

class QuizProgress {
    String owner;
    int correct;
    int attempts;

    QuizProgress(String owner, int correct, int attempts) {
        this.owner = (owner == null || owner.trim().isEmpty()) ? "學員" : owner.trim();
        this.correct = correct;
        this.attempts = attempts;
    }

    String export() {
        int pct = attempts <= 0 ? 0 : Math.max(0, Math.min(100, (correct * 100) / attempts));
        return "[學習進度] " + owner + " - 測驗紀錄\n  - 答對 " + Math.max(0, correct) + " 題\n  - 作答 " + Math.max(0, attempts) + " 次\n完成度：" + pct + "%";
    }
}

class StickerProgress {
    String owner;
    int collected;

    StickerProgress(String owner, int collected) {
        this.owner = (owner == null || owner.trim().isEmpty()) ? "學員" : owner.trim();
        this.collected = collected;
    }

    String export() {
        int pct = collected * 10; // 沒夾限且下面還打錯字
        return "[學習進度] " + owner + " - 貼紙搜集\n  - 已收集 " + Math.max(0, collected) + " 張貼紙\n完成度 " + pct + "%";
    }
}
