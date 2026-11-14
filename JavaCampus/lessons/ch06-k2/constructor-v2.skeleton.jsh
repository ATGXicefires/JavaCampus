class Item {
    String name;
    String kind;
    int price;
    int stock;

    Item(String name, String kind, int price, int stock) {
        if (name == null || name.trim().isEmpty()) {
            this.name = "未命名";
        } else {
            this.name = name;
        }
        if (kind == null || kind.trim().isEmpty()) {
            this.kind = "扭蛋";
        } else {
            this.kind = kind;
        }
        this.price = ____;
        this.stock = ____;
    }

    int totalValue() {
        return price * stock;
    }
}

Item[] bag = new Item[names.length];
for (int i = 0; i < names.length; i++) {
    bag[i] = new Item(names[i], "扭蛋", ____, stocks[i]);
}

static int totalBagValue(Item[] bag) {
    if (bag == null) {
        return 0;
    }
    int sum = 0;
    for (int i = 0; i < bag.length; i++) {
        if (bag[i] != ____) {
            sum += bag[i].totalValue();
        }
    }
    return sum;
}
