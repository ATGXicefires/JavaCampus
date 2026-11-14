// 國中寫的 Python 版本：balance 誰都能改
// balance = 0
// balance = balance + 350
// balance = balance + 120
class Ledger {
    private final String owner;
    private int balance;
    private int entryCount;
    private int totalDeposited;
    private boolean closed;

    Ledger() {
        this("阿嬤小吃店");
    }

    Ledger(String owner) {
        if (owner == null || owner.isBlank()) {
            this.owner = "未命名帳本";
        } else {
            this.owner = owner.trim();
        }
    }

    boolean deposit(int amount) {
        if (closed || amount <= 0) {
            return false;
        }
        balance += amount;
        totalDeposited += amount;
        entryCount++;
        return true;
    }

    void close() {
        closed = true;
    }

    boolean isClosed() {
        return closed;
    }

    int getBalance() {
        return balance;
    }

    int getEntryCount() {
        return entryCount;
    }

    int getTotalDeposited() {
        return totalDeposited;
    }

    String getOwner() {
        return owner;
    }
}

Ledger book = new Ledger("阿嬤小吃店");
book.deposit(350);
book.deposit(120);
book.deposit(80);
