interface Drawable {
    void draw();
}

class Button implements Drawable {
    public void draw() {
        System.out.println("[ 開始學習 ]");
    }
}

Button start = new Button();
start.draw();
