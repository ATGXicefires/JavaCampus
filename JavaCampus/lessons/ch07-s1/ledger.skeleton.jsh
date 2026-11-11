class Ledger {
    ____ int balance;

    void deposit(int amount) {
        balance += amount;
    }

    int getBalance() {
        return balance;
    }
}

Ledger book = new Ledger();
book.deposit(350);
book.____(120);
