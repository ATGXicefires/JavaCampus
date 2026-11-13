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
        this.price = price < 0 ? 0 : price;
        this.stock = stock < 0 ? 0 : stock;
    }

    int totalValue() {
        return price * stock;
    }

    boolean restock(int n) {
        if (n <= 0) {
            return false;
        }
        stock += n;
        return true;
    }

    boolean sell(int n) {
        if (n <= 0 || n > stock) {
            return false;
        }
        stock -= n;
        return true;
    }

    boolean transferTo(Item target, int n) {
        if (target == null || target == ____ || n <= 0 || ____) {
            return false;
        }
        stock -= n;
        target.stock += n;
        return true;
    }
}

Item laptop = new Item("小白", "筆電", 32000, 1);
Item usb = new Item("阿隨", "USB", 450, 2);
