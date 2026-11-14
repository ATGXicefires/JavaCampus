// 國中寫的 Python 版本：balance 誰都能改
// balance = 0
// balance = balance + 350
// balance = balance + 120
class Ledger {
    // 宣告 private final String owner 與 private int balance, entryCount, totalDeposited

    Ledger() {
    }

    Ledger(String owner) {
    }

    boolean deposit(int amount) {
        return false;
    }

    int getBalance() {
        return 0;
    }

    int getEntryCount() {
        return 0;
    }

    int getTotalDeposited() {
        return 0;
    }

    String getOwner() {
        return "";
    }
}

// 建立 Ledger book = new Ledger("阿嬤小吃店");，存入 350 和 120
