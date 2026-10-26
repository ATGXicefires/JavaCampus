interface Drawable {
    void draw();
}

class Button implements Drawable {
    public void draw() {
        System.out.println("[ 開始學習 ]");
    }
}

class ProgressBar implements Drawable {
    int done;
    int total;

    ProgressBar(int done, int total) {
        this.done = done;
        this.total = total;
    }

    public void draw() {
        System.out.println("■".repeat(done) + "□".repeat(total - done));
    }
}

// 小老師的元件：class MascotView implements Drawable，draw() 印出 滷肉：「歡迎回來！」
// 按鈕、進度條 new ProgressBar(3, 5)、小老師依序放進 Drawable[] screen，用 for 全部畫出來
