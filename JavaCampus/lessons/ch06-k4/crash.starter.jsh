class Mascot {
    String name;
    String line;

    Mascot(String name, String line) {
        this.name = name;
        this.line = line;
    }
}

Mascot[] mascots = {
    new Mascot("滷肉", "再跑一次！"),
    null,
    new Mascot(null, "嗨"),
    new Mascot("分號", null)
};

// Bug：未處理 list 為 null、陣列中有 null 元素、或 name 為 null/空白的情況
static int validCount(Mascot[] list) {
    int count = 0;
    for (int i = 0; i < list.length; i++) {
        if (!list[i].name.trim().isEmpty()) {
            count++;
        }
    }
    return count;
}

// Bug：m、m.name、m.line 都可能是 null 或空白
static String describe(Mascot m) {
    String showName = m.name.trim().isEmpty() ? "未取名" : m.name;
    String showLine = m.line.trim().isEmpty() ? "（安靜）" : m.line;
    return showName + "：" + showLine;
}
