class Draft {
    private String text = "";

    void type(String s) {
        text += s;
    }

    void clear() {
        text = ____;
    }

    String preview() {
        return text;
    }
}

Draft draft = new Draft();
draft.type("學長");
draft.____();
draft.type("新年快樂");
