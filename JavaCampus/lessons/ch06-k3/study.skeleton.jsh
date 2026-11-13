class Mascot {
    String name;
    String line;
    int energy;
    int mood;
    int level;

    Mascot(String name, String line) {
        if (name == null || name.trim().isEmpty()) {
            this.name = "新同學";
        } else {
            this.name = name;
        }
        this.line = line == null ? "" : line;
        this.energy = 60;
        this.mood = 50;
        this.level = 1;
    }

    String speak() {
        if (energy < 10) {
            return name + "：「……（沒力了）」";
        }
        energy -= 10;
        mood = Math.min(100, mood + 5);
        return name + "：「" + line + "」";
    }

    void rest(int minutes) {
        if (minutes <= 0) {
            return;
        }
        int units = minutes / 10;
        energy = Math.min(100, energy + units * 5);
        mood = Math.min(100, mood + units * 2);
    }

    boolean study(int hours) {
        if (hours <= 0) {
            return false;
        }
        int cost = hours * 20;
        if (energy < cost) {
            mood = Math.max(0, mood - ____);
            return false;
        }
        energy -= cost;
        int bonus = mood >= ____ ? 1 : 0;
        level += hours + bonus;
        mood = Math.min(100, mood + hours * 10);
        return true;
    }

    String greet(String user) {
        String target = (user == null || user.trim().isEmpty()) ? "新同學" : user;
        return name + "（Lv." + level + "）：「" + ____ + "，今天體力 " + energy + "、心情 " + mood + "！」";
    }
}
