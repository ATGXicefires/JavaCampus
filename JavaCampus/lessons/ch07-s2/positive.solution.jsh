class Ledger {
    static final int MAX_SINGLE_DEPOSIT = 50000;
    private int balance;
    private int rejectedCount;

    boolean deposit(int amount) {
        if (amount <= 0 || amount > MAX_SINGLE_DEPOSIT) {
            rejectedCount++;
            return false;
        }
        balance += amount;
        return true;
    }

    int getBalance() {
        return balance;
    }

    int getRejectedCount() {
        return rejectedCount;
    }
}
