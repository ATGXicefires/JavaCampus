class Ledger {
    static final int MAX_SINGLE_DEPOSIT = 50000;
    private int balance;
    private int rejectedCount;

    Ledger() {
    }

    Ledger(int start) {
        if (start < 0) {
            balance = 0;
        } else {
            balance = start;
        }
    }

    boolean deposit(int amount) {
        if (amount <= 0 || amount > MAX_SINGLE_DEPOSIT) {
            rejectedCount++;
            return false;
        }
        balance += amount;
        return true;
    }

    boolean withdraw(int amount) {
        if (amount <= 0 || amount > balance) {
            rejectedCount++;
            return false;
        }
        balance -= amount;
        return true;
    }

    int getBalance() {
        return balance;
    }

    int getRejectedCount() {
        return rejectedCount;
    }
}
