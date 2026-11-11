class GachaBag {
    private int count;

    void add() {
        count++;
    }

    int getCount() {
        return count;
    }
}

GachaBag bag = new GachaBag();
for (int i = 0; i < 8; i++) {
    bag.add();
}
