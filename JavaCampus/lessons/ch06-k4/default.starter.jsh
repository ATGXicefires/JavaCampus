class Mascot {
    String name;
    String line;

    // Bug：沒有處理 name 為 null/空白，也沒有處理 line 為 null
    Mascot(String name, String line) {
        this.name = name;
        this.line = line;
    }

    // Bug：target 為 null 或 this.name 為 null 時會崩潰，且未去掉空白
    boolean hasName(String target) {
        return name.equals(target);
    }

    // Bug：other 為 null 或 other.line 為 null/空白時沒有防護
    boolean copyLineFrom(Mascot other) {
        this.line = other.line;
        return true;
    }
}
