class Mascot {
    String name;
    String line;

    Mascot(String name, String line) {
        if (name == null || name.trim().isEmpty()) {
            this.name = "未命名";
        } else {
            this.name = name.trim();
        }
        this.line = line == null ? "" : line;
    }

    void rename(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    void updateLine(String line) {
        if (line != null) {
            this.line = line;
        }
    }

    String speakText() {
        return name + "：「" + line + "」";
    }
}
