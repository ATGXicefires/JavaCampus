public class ProgressBar {
    int done;
    int total;

    public ProgressBar(int done, int total) {
        this.done = done;
        this.total = total;
    }

    public String bar() {
        ____
    }

    public int percent() {
        return done * 100 / total;
    }
}
