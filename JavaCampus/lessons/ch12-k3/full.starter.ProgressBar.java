public class ProgressBar {
    int done;
    int total;

    ProgressBar(int done, int total) {
        this.done = done;
        this.total = total;
    }

    String bar() {
        return "■".repeat(done) + "□".repeat(total - done);
    }

    int percent() {
        return done * 100 / total;
    }
}
