class Ledger {
    static final int MAX_SINGLE_DEPOSIT = 50000;
    private int balance;
    private int rejectedCount;
    private boolean locked;

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
        if (locked) {
            return false;
        }
        if (amount <= 0 || amount > MAX_SINGLE_DEPOSIT) {
            rejectedCount++;
            if (rejectedCount >= 3) {
                locked = true;
            }
            return false;
        }
        balance += amount;
        return true;
    }

    boolean withdraw(int amount) {
        if (locked) {
            return false;
        }
        if (amount <= 0 || amount > balance) {
            rejectedCount++;
            if (rejectedCount >= 3) {
                locked = true;
            }
            return false;
        }
        balance -= amount;
        return true;
    }

    boolean isLocked() {
        return locked;
    }

    boolean unlock(String code) {
        if (!"GRANDMA".equals(code)) {
            return false;
        }
        locked = false;
        rejectedCount = 0;
        return true;
    }

    int getBalance() {
        return balance;
    }

    int getRejectedCount() {
        return rejectedCount;
    }
}

Ledger book = new Ledger(500);
int amount = -350;
if (!book.deposit(amount)) {
    System.out.println("金額不對：" + amount);
}
