class Ledger {
    private int balance;

    void deposit(int amount) {
        balance += amount;
    }

    int getBalance() {
        return balance;
    }
}

// 加上 static final int MAX_SINGLE_DEPOSIT = 50000; 與 private int rejectedCount;
// 將 deposit 改成交回 boolean：擋掉 <= 0 或 > MAX_SINGLE_DEPOSIT，被擋時累加 rejectedCount
// 加上 getRejectedCount()
