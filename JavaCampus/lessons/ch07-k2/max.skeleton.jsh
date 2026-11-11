class GachaBag {
    static final int MAX = 12;
    private int count;

    GachaBag(int count) {
        ____.count = count;
    }

    boolean add() {
        if (count >= ____) {
            return false;
        }
        count++;
        return true;
    }

    int getCount() {
        return count;
    }
}
