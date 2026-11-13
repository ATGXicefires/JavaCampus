class Ledger {
    static final int MAX_SINGLE_DEPOSIT = 50000;
    private int balance;
    private int rejectedCount;

    boolean deposit(int amount) {
        if (amount <= 0 || amount > ____) {
            rejectedCount++;
            return false;
        }
        balance += amount;
        return ____;
    }

    int getBalance() {
        return balance;
    }

    int getRejectedCount() {
        return rejectedCount;
    }
}
