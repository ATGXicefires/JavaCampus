class Table {
    private static int todayTotal;
    private static int todayOrders;
    private int amount;
    private int itemCount;
    private boolean settled;

    boolean order(int price) {
        if (____ || price <= 0) {
            return false;
        }
        amount += price;
        itemCount++;
        todayTotal += price;
        todayOrders++;
        return true;
    }

    int settle() {
        settled = true;
        return amount;
    }

    boolean isSettled() {
        return settled;
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

for (int i = 0; i < orders.length; i++) {
    Table t = new Table();
    t.order(____);
    t.settle();
}
