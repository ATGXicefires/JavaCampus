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

    String greet(String user) {
        return name + "：「" + ____ + "，歡迎回來！」";
    }
}

Mascot roufan = new Mascot("滷肉", "沒關係，再跑一次！");
Mascot semi = new Mascot("分號", "分號。");
roufan.speak();
semi.speak();
