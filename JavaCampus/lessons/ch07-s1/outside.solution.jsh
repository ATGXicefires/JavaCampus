// 國中寫的 Python 版本：balance 誰都能改
// balance = 0
// balance = balance + 350
// balance = balance + 120
class Ledger {
    private int balance;

    void deposit(int amount) {
        balance += amount;
    }

    int getBalance() {
        return balance;
    }
}

Ledger book = new Ledger();
book.deposit(350);
book.deposit(120);
book.deposit(80);
