class GachaBag {
    private String pin;
    private int count;
    private int wrongTries;
    private boolean opened;
    private boolean lockedOut;

    GachaBag(String pin) {
        if (pin == null || pin.isEmpty()) {
            this.pin = "0000";
        } else {
            this.pin = pin;
        }
    }

    boolean unlock(String input) {
        if (lockedOut) {
            return false;
        }
        if (pin.equals(input)) {
            opened = true;
            wrongTries = 0;
            return true;
        }
        opened = false;
        wrongTries++;
        if (wrongTries >= 3) {
            lockedOut = true;
        }
        return false;
    }

    void lock() {
        opened = false;
    }

    boolean add() {
        if (lockedOut || !opened) {
            return false;
        }
        count++;
        return true;
    }

    boolean resetLock(String masterCode, String newPin) {
        if (!"MASTER-07".equals(masterCode) || newPin == null || newPin.isEmpty()) {
            return false;
        }
        this.pin = newPin;
        this.lockedOut = false;
        this.wrongTries = 0;
        this.opened = false;
        return true;
    }

    int getCount() {
        return count;
    }

    boolean isOpened() {
        return opened;
    }

    boolean isLockedOut() {
        return lockedOut;
    }
}

GachaBag bag = new GachaBag("0718");
bag.unlock("0718");
for (int i = 0; i < 8; i++) {
    bag.add();
}
bag.add();
