class Gacha {
    String name;
    String color;
}

for (int i = 0; i < names.length; i++) {
    Gacha g = new Gacha();
    g.name = names[i];
    System.out.println("第 " + (i + 1) + " 個：" + g.name);
}
