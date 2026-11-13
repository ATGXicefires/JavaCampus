class Mascot {
    String name;
    String line;

    // Bug：參數遮蔽了欄位，且未處理 null / 空白
    Mascot(String name, String line) {
        name = name;
        line = line;
    }

    // Bug：只改到了區域參數 name
    void rename(String name) {
        if (name != null && !name.trim().isEmpty()) {
            name = name.trim();
        }
    }

    // Bug：只改到了區域參數 line
    void updateLine(String line) {
        if (line != null) {
            line = line;
        }
    }

    String speakText() {
        return name + "：「" + line + "」";
    }
}
