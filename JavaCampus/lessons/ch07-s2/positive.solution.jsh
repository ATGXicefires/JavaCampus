class Ledger {
    private int balance;

    boolean deposit(int amount) {
        if (amount <= 0) {
            return false;
        }
        balance += amount;
        return true;
    }

    int getBalance() {
        return balance;
    }
}
