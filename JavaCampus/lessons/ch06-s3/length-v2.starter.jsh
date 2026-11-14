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
        return name.length();
    }

    String greet(String user) {
        String lastListener = user.trim();
        return name + "：「" + lastListener + "，嗨！」";
    }

    void resetStats() {
        speakCount = 0;
    }
}
