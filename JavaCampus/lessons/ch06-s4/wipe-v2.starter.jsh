class Glasses {
    int smudges = 40;
    int wipeCount = 0;
    int wearMinutes = 0;
    boolean clothReady = true;

    boolean isClear() {
        return smudges <= 20;
    }

    String view() {
        return isClear() ? "世界很清楚" : "世界霧霧的";
    }

    void wear(int minutes) {
        if (minutes <= 0) {
            return;
        }
        this.wearMinutes += minutes;
        this.smudges = Math.min(100, this.smudges + minutes / 5);
    }

    boolean wipe() {
        if (!clothReady) {
            return false;
        }
        smudges = Math.max(0, smudges - 30);
        wipeCount++;
        if (wipeCount % 3 == 0) {
            clothReady = false;
        }
        return true;
    }

    void washCloth() {
        boolean clothReady = true;
    }

    void resetDaily() {
        wearMinutes = 0;
    }

    String summary() {
        return "";
    }
}

// TODO：心跳加速的原因。之後再處理。
