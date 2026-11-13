class Table {
    private static int todayTotal;
    private static int todayOrders;
    private int amount;
    private int itemCount;

    boolean order(int price) {
        if (price <= 0) {
            return false;
        }
        amount += price;
        itemCount++;
        todayTotal += price;
        todayOrders++;
        return true;
    }

    int getAmount() {
        return amount;
    }

    int getItemCount() {
        return itemCount;
    }

    static int getTodayTotal() {
        return todayTotal;
    }

    static int getTodayOrders() {
        return todayOrders;
    }

    static void resetDay() {
        todayTotal = 0;
        todayOrders = 0;
    }
}

Table t1 = new Table();
t1.order(80);
t1.order(35);
Table t2 = new Table();
t2.order(120);
