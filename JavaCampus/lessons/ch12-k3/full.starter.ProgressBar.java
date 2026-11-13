public class ProgressBar {
    int done;
    int total;

    public ProgressBar(int done, int total) {
        this.done = done;
        this.total = total;
    }

    public String bar() {
        return "■".repeat(done) + "□".repeat(total - done);
    }

    public int percent() {
        return done * 100 / total;
    }
}
