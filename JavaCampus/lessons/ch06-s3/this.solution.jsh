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
}

Mascot roufan = new Mascot("滷肉", "沒關係，再跑一次！");
roufan.speak();
