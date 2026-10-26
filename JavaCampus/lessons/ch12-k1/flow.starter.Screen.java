public class Screen implements Drawable {
    String title;

    Screen(String title) {
        this.title = title;
    }

    public void draw() {
        System.out.println("== " + title + " ==");
    }
}
