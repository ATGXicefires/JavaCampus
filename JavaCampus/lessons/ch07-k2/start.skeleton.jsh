class GachaBag {
    static final int MAX = 12;
    private int count;

    GachaBag(int count) {
        if (count > MAX) {
            this.count = ____;
        } else if (count < 0) {
            this.count = ____;
        } else {
            this.count = count;
        }
    }

    boolean add() {
        if (count >= MAX) {
            return false;
        }
        count++;
        return true;
    }

    int getCount() {
        return count;
    }
}
