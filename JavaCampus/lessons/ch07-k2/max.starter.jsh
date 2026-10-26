class GachaBag {
    private int count;

    void add() {
        count++;
    }

    int getCount() {
        return count;
    }
}

// 加上常數 MAX = 12、建構子 GachaBag(int count)
// add() 改成交回 boolean：還沒滿就加一個、交回 true；滿了交回 false
