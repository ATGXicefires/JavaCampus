class Draft {
    // 加上 static final int MAX_LEN = 20 與 private 欄位 text、editCount

    boolean type(String s) {
        return false;
    }

    void clear() {
    }

    String preview() {
        return "";
    }

    int getEditCount() {
        return 0;
    }

    int remainingChars() {
        return 0;
    }
}

// 建立 Draft draft = new Draft();，依序呼叫 type("學長")、clear()、type("新年快樂")
