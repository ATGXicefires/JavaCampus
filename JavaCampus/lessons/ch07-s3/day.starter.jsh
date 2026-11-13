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

    // 補上 private boolean settled、settle()、isSettled()，並讓 order 拒絕已結帳的桌次

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

// 用 for 走過 orders：每筆 new 一桌，呼叫 order 與 settle
