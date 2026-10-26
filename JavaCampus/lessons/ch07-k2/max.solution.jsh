class GachaBag {
    static final int MAX = 12;
    private int count;

    GachaBag(int count) {
        this.count = count;
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
