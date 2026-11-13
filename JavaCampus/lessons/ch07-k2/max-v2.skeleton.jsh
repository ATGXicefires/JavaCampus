class GachaBag {
    static final int MAX = 12;
    private int count;

    GachaBag(int count) {
        this.count = count;
    }

    boolean add() {
        if (count >= ____) {
            return false;
        }
        count++;
        return true;
    }

    int addUpTo(int n) {
        if (n <= 0) {
            return 0;
        }
        int room = MAX - count;
        int added = Math.min(n, room);
        count += ____;
        return added;
    }

    int take(int n) {
        if (n <= 0) {
            return 0;
        }
        int taken = Math.min(n, count);
        count -= taken;
        return taken;
    }

    int getCount() {
        return count;
    }
}
