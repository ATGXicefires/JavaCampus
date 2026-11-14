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
            this.kind = "未分類";
        } else {
            this.kind = kind;
        }
        this.price = ____;
        this.stock = ____;
    }

    int totalValue() {
        return ____;
    }
}

Item laptop = new Item("小白", "筆電", 32000, 1);
Item usb = ____ Item("阿隨", "USB", 450, 2);
