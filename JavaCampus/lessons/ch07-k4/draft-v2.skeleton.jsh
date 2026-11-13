class Draft {
    static final int MAX_LEN = 20;
    private String text = "";
    private int editCount;

    boolean type(String s) {
        if (s == null || s.isEmpty() || text.length() + s.length() > MAX_LEN) {
            return false;
        }
        text += s;
        editCount++;
        return true;
    }

    void clear() {
        if (!text.isEmpty()) {
            text = ____;
            editCount++;
        }
    }

    String preview() {
        return text;
    }

    int getEditCount() {
        return editCount;
    }

    int remainingChars() {
        return MAX_LEN - text.length();
    }
}

Draft draft = new Draft();
draft.type("學長");
draft.clear();
draft.type("新年快樂");
