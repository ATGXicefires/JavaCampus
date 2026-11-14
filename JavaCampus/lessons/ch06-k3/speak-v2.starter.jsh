class Mascot {
    String name;
    String line;
    int energy;
    int mood;
    int level;

    // 建構子：name 為 null 或空白時預設 "新同學"；line 為 null 時預設 ""
    // 初始值：energy = 60, mood = 50, level = 1
    Mascot(String name, String line) {

    }

    // 體力不足 10 時回傳 "<name>：「……（沒力了）」"；
    // 否則 energy - 10，mood + 5（上限 100），回傳 "<name>：「<line>」"
    String speak() {
        return "";
    }

    // minutes <= 0 不處理；每滿 10 分鐘恢復 5 點體力與 2 點心情（上限 100）
    void rest(int minutes) {

    }
}
