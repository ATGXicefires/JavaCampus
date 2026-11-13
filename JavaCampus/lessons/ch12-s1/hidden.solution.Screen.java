public class Screen {
    String title;

    public Screen(String title) {
        this.title = title;
    }

    public void show() {
        System.out.println("== " + title + " ==");
    }
}
