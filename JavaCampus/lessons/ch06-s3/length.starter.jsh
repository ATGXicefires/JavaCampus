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

    // Bug 1：name 為 null 時會拋出 NullPointerException
    int nameLength() {
        return name.length();
    }

    // Bug 2：user 為 null 時會崩潰；誤宣告區域變數遮蔽欄位；漏掉 speakCount 累加；name 可能為 null
    String greet(String user) {
        String lastListener = user.trim();
        return name + "：「" + lastListener + "，嗨！」";
    }

    // Bug 3：狀態重置不完整
    void resetStats() {
        speakCount = 0;
    }
}
