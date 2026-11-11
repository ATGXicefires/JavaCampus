class Item {
    String name;
    String kind;

    Item(String name, String kind) {
        this.name = name;
        this.kind = kind;
    }
}

Item[] bag = new Item[names.length];
for (int i = 0; i < names.length; i++) {
    bag[i] = new Item(names[i], "扭蛋");
}
for (int i = 0; i < bag.length; i++) {
    System.out.println(bag[i].name + "（" + bag[i].kind + "）");
}
