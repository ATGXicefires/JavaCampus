class Gacha {
    String name;
    String color;
    int price;
    int stock;

    Gacha(String name, String color, int price, int stock) {
        if (name == null || name.trim().isEmpty()) {
            this.name = "未命名";
        } else {
            this.name = name;
        }
        if (color == null || color.trim().isEmpty()) {
            this.color = "透明";
        } else {
            this.color = color;
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

    boolean draw(int n) {
        if (n <= 0 || n > stock) {
            return false;
        }
        stock -= n;
        return true;
    }
}

Gacha[] box = new Gacha[names.length];
for (int i = 0; i < names.length; i++) {
    box[i] = new Gacha(____, "限定色", 60, (i % 3) + 1);
    System.out.println("第 " + (i + 1) + " 個：" + box[i].name + "（剩 " + box[i].stock + " 顆）");
}

static int totalBoxValue(Gacha[] box) {
    if (box == null) {
        return 0;
    }
    int sum = 0;
    for (int i = 0; i < box.length; i++) {
        if (box[i] != ____) {
            sum += box[i].totalValue();
        }
    }
    return sum;
}
