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

static int validCount(Mascot[] list) {
    if (list == null) {
        return 0;
    }
    int count = 0;
    for (int i = 0; i < list.length; i++) {
        Mascot m = list[i];
        if (m != null && m.name != null && !m.name.trim().isEmpty()) {
            count++;
        }
    }
    return count;
}

static String describe(Mascot m) {
    if (m == null) {
        return "空位";
    }
    String showName = (m.name == null || m.name.trim().isEmpty()) ? "未取名" : m.name;
    String showLine = (m.line == null || m.line.trim().isEmpty()) ? "（安靜）" : m.line;
    return showName + "：" + showLine;
}
