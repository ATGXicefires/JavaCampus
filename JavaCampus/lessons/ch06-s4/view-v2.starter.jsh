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
        int wearMinutes = this.wearMinutes + minutes;
    }

    boolean wipe() {
        smudges = 0;
        return true;
    }
}
