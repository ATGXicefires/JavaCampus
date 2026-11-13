class GachaBag {
    static final int MAX = 12;
    private final String label;
    private int count;

    GachaBag(int count) {
        this("基本款", count);
    }

    GachaBag(String label, int count) {
        if (label == null || label.isBlank()) {
            this.label = "未命名";
        } else {
            this.label = label.trim();
        }
        if (count > MAX) {
            this.count = MAX;
        } else if (count < 0) {
            this.count = 0;
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

    int addUpTo(int n) {
        if (n <= 0) {
            return 0;
        }
        int room = MAX - count;
        int added = Math.min(n, room);
        count += added;
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

    // 補上 remainingSpace() 和 isFull()

    String getLabel() {
        return label;
    }

    int getCount() {
        return count;
    }
}

GachaBag.MAX = 20;
