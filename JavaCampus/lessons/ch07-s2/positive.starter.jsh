class Ledger {
    private int balance;

    void deposit(int amount) {
        balance += amount;
    }

    int getBalance() {
        return balance;
    }
}

// deposit 改成交回 boolean：0 或負數就不存、交回 false
