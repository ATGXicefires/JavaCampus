class Table {
    static int todayTotal;
    private int amount;

    void order(int price) {
        amount += price;
        todayTotal += price;
    }

    int getAmount() {
        return amount;
    }
}

// 用 for 把 orders 每一筆都點在一桌新的客人上
