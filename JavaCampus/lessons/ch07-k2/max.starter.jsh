class GachaBag {
    private int count;

    void add() {
        count++;
    }

    int getCount() {
        return count;
    }
}

// 加上常數 MAX = 12、建構子 GachaBag(int count)：把一開始有幾個存進 count
// add() 改成交回 boolean：還不到 MAX 個就加一個、交回 true；滿了就不加、交回 false
