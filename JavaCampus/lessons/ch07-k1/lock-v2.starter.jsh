class GachaBag {
    // 宣告所需的 private 欄位：密碼、扭蛋數量、連續錯誤次數、開鎖狀態、鎖死狀態

    GachaBag(String pin) {
        // 若 pin 為 null 或空字串，預設為 "0000"
    }

    boolean unlock(String input) {
        return false;
    }

    void lock() {
    }

    boolean add() {
        return false;
    }

    int getCount() {
        return 0;
    }

    boolean isOpened() {
        return false;
    }

    boolean isLockedOut() {
        return false;
    }
}

// 建立 GachaBag bag = new GachaBag("0718");，解鎖後放入 8 個扭蛋
