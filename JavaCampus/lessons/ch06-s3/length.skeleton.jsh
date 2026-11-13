class Mascot {
    String name;
    String line;
    int speakCount;
    String lastListener;

    Mascot(String name, String line) {
        if (name == null || name.trim().isEmpty()) {
            this.name = "未命名";
        } else {
            this.name = name.trim();
        }
        this.line = line == null ? "" : line;
    }

    int nameLength() {
        if (this.name == ____) {
            return 0;
        }
        return this.name.length();
    }

    String greet(String user) {
        String target = (user == null || user.trim().isEmpty()) ? "新同學" : user.trim();
        ____.lastListener = target;
        speakCount++;
        String who = this.name == null ? "未命名" : this.name;
        return who + "：「" + target + "，嗨！」";
    }

    void resetStats() {
        speakCount = 0;
        ____ = null;
    }
}
