class GachaBag {
    private int count;

    void add() {
        count++;
    }

    int getCount() {
        return count;
    }
}

// 加上 static final int MAX = 12 與建構子 GachaBag(int count)
// 實作 boolean add()、int addUpTo(int n)、int take(int n) 保護 0..MAX 範圍
