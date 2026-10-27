class GachaBag {
    private int count;

    void add() {
        count++;
    }

    int getCount() {
        return count;
    }
}

// 加上 static final int MAX = 12，和建構子 GachaBag(int count)：把一開始有幾個存進欄位 count
// add() 交回 boolean：還不到 MAX 個就加一個、交回 true；已經有 MAX 個就不加、交回 false
