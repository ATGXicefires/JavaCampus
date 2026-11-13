class Mascot {
    String name;
    String line;

    Mascot(String name, String line) {
        name = name;
        line = line;
    }

    void rename(String name) {
        if (name != null && !name.trim().isEmpty()) {
            name = name.trim();
        }
    }

    void updateLine(String line) {
        if (line != null) {
            line = line;
        }
    }

    String speakText() {
        return name + "：「" + line + "」";
    }
}
