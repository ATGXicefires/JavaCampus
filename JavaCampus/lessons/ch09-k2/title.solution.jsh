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

class MascotView implements Drawable {
    public void draw() {
        System.out.println("滷肉：「歡迎回來！」");
    }
}

class Title implements Drawable {
    public void draw() {
        System.out.println("== 小白學程式 ==");
    }
}

Drawable[] screen = {new Title(), new Button(), new ProgressBar(3, 5), new MascotView()};
for (int i = 0; i < screen.length; i++) {
    screen[i].draw();
}
