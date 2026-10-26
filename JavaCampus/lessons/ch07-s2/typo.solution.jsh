class Ledger {
    private int balance;

    Ledger(int start) {
        balance = start;
    }

    boolean deposit(int amount) {
        if (amount <= 0) {
            return false;
        }
        balance += amount;
        return true;
    }

    boolean withdraw(int amount) {
        if (amount > balance) {
            return false;
        }
        balance -= amount;
        return true;
    }

    int getBalance() {
        return balance;
    }
}

Ledger book = new Ledger(500);
int amount = -350;
if (!book.deposit(amount)) {
    System.out.println("金額不對：" + amount);
}
