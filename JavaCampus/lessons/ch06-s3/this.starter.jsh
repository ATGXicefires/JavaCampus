class Mascot {
    String name;
    String line;

    Mascot(String name, String line) {
        // 把傳進來的名字和台詞放進欄位
        name = name;
        line = line;
    }

    void speak() {
        System.out.println(name + "：「" + line + "」");
    }
}

Mascot roufan = new Mascot("滷肉", "沒關係，再跑一次！");
roufan.speak();
