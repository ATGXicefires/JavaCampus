class Draft {
    private String text = "";

    void type(String s) {
        text += s;
    }

    void clear() {
        text = "";
    }

    String preview() {
        return text;
    }
}

Draft draft = new Draft();
draft.type("學長");
draft.clear();
draft.type("新年快樂");
