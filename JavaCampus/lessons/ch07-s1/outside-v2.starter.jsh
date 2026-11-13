// 國中寫的 Python 版本：balance 誰都能改
// balance = 0
// balance = balance + 350
// balance = balance + 120
class Ledger {
    private final String owner;
    private int balance;
    private int entryCount;
    private int totalDeposited;

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
        if (amount <= 0) {
            return false;
        }
        balance += amount;
        totalDeposited += amount;
        entryCount++;
        return true;
    }

    // 加上 close()、isClosed()，並讓 deposit 在封帳後拒絕入帳

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
book.balance = 0;
