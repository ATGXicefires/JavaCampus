public class ProgressBar {
    int done;
    int total;

    ProgressBar(int done, int total) {
        this.done = done;
        this.total = total;
    }

    String bar() {
        int filled = done;
        if (filled > total) {
            filled = total;
        }
        return "■".repeat(filled) + "□".repeat(total - filled);
    }

    int percent() {
        return done * 100 / total;
    }
}
