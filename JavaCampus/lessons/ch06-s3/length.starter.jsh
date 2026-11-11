class Mascot {
    String name;
    String line;

    Mascot(String name, String line) {
        this.name = name;
        this.line = line;
    }

    void speak() {
        System.out.println(name + "：「" + line + "」");
    }

    // 交回名字有幾個字；名字是 null 就交回 0
    int nameLength() {
        return name.length();
    }
}

Mascot roufan = new Mascot("滷肉", "沒關係，再跑一次！");
roufan.speak();
