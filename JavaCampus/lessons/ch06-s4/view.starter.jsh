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

    // Bug：沒有擋負數、誤宣告區域變數遮蔽 wearMinutes、且忘了增加 smudges
    void wear(int minutes) {
        int wearMinutes = this.wearMinutes + minutes;
    }

    // Bug：沒有檢查 clothReady、直接把 smudges 歸 0、也沒有計數與弄髒擦拭布
    boolean wipe() {
        smudges = 0;
        return true;
    }
}
