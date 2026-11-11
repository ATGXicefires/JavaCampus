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

// 用 for 照 orders 一筆一筆來：每一筆都 new 一桌，再用 order 點那一筆
