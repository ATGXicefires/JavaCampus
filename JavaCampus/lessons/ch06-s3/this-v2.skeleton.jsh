class Mascot {
    String name;
    String line;

    Mascot(String name, String line) {
        if (name == null || name.trim().isEmpty()) {
            ____.name = "未命名";
        } else {
            ____.name = name.trim();
        }
        this.line = line == null ? "" : ____;
    }

    void rename(String name) {
        if (name != null && !name.trim().isEmpty()) {
            ____.name = name.trim();
        }
    }

    void updateLine(String line) {
        if (line != null) {
            ____.line = line;
        }
    }

    String speakText() {
        return name + "：「" + line + "」";
    }
}
