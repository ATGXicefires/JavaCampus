class Mascot {
    String name;
    String line;
    int energy;
    int mood;
    int level;

    Mascot(String name, String line, int energy, int mood) {
        if (name == null || name.trim().isEmpty()) {
            this.name = "小老師";
        } else {
            this.name = name;
        }
        this.line = line == null ? "" : line;
        this.energy = Math.max(0, Math.min(100, ____));
        this.mood = Math.max(0, Math.min(100, ____));
        this.level = 1;
    }

    Mascot(String name, String line) {
        this(name, line, ____, ____);
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
}
