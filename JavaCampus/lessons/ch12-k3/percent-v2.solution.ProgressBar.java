public class ProgressBar {
    int done;
    int total;

    public ProgressBar(int done, int total) {
        this.done = done;
        this.total = total;
    }

    public String bar() {
        if (total <= 0) {
            return "";
        }
        int filled = done;
        if (filled < 0) {
            filled = 0;
        } else if (filled > total) {
            filled = total;
        }
        return "■".repeat(filled) + "□".repeat(total - filled);
    }

    public int percent() {
        if (total <= 0 || done <= 0) {
            return 0;
        }
        if (done >= total) {
            return 100;
        }
        return (int) Math.round(done * 100.0 / total);
    }
}
