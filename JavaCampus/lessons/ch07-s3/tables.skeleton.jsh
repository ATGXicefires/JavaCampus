class Table {
    ____ int todayTotal;
    private int amount;

    void order(int price) {
        amount += price;
        todayTotal += ____;
    }

    int getAmount() {
        return amount;
    }
}

Table t1 = new Table();
t1.order(80);
t1.order(35);
Table t2 = new Table();
t2.order(120);
