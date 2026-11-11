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

for (int i = 0; i < orders.length; i++) {
    Table t = new Table();
    t.order(orders[i]);
}
