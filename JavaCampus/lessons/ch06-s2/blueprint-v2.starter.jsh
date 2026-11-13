class Mascot {
    String name;
    String line;
    int energy;
    int mood;
    int level;

    // 建構子：name 為 null 或空白時設為 "小老師"，line 為 null 時設為 ""
    // energy 與 mood 限制在 0..100；level 初始為 1
    Mascot(String name, String line, int energy, int mood) {

    }

    // 預設以 energy = 60、mood = 50 建構
    Mascot(String name, String line) {

    }

    // energy < 10 回傳 "<name>：「……（沒力了）」"；
    // 否則 energy - 10、mood + 5（上限 100），回傳 "<name>：「<line>」"
    String speak() {
        return "";
    }

    // minutes <= 0 不處理；每滿 10 分鐘恢復 5 點 energy 與 2 點 mood（上限 100）
    void rest(int minutes) {

    }
}
