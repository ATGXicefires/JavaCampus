class ProgressBar implements ____ {
    int done;
    int total;

    ProgressBar(int done, int total) {
        ____
    }

    public boolean isReady() {
        return ____;
    }

    public String render(int maxWidth) {
        if (maxWidth <= 0) {
            return "";
        }
        ____
    }
}
