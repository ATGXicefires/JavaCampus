public class Screen {
    String title;

    Screen(String title) {
        this.title = title;
    }

    void show() {
        System.out.println("== " + title + " ==");
    }
}
