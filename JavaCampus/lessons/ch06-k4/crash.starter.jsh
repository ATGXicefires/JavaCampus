class Mascot {
    String name;
    String line;

    Mascot(String name, String line) {
        this.name = name;
        this.line = line;
    }
}

Mascot[] mascots = {new Mascot("滷肉", "沒關係，再跑一次！"), new Mascot("分號", "分號。"), new Mascot(null, "嗨")};
for (int i = 0; i < mascots.length; i++) {
    System.out.println(mascots[i].name + "：" + mascots[i].name.length() + " 個字");
}
