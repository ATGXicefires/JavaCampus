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

Button start = new Button();
start.draw();
ProgressBar bar = new ProgressBar(3, 5);
bar.draw();
