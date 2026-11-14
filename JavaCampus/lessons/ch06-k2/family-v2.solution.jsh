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
        this.price = price < 0 ? 0 : price;
        this.stock = stock < 0 ? 0 : stock;
    }

    int totalValue() {
        return price * stock;
    }
}

Item[] bag = new Item[names.length];
for (int i = 0; i < names.length; i++) {
    bag[i] = new Item(names[i], "扭蛋", prices[i], stocks[i]);
}

static int totalBagValue(Item[] bag) {
    if (bag == null) {
        return 0;
    }
    int sum = 0;
    for (int i = 0; i < bag.length; i++) {
        if (bag[i] != null) {
            sum += bag[i].totalValue();
        }
    }
    return sum;
}

static String lowestStockName(Item[] bag) {
    if (bag == null) {
        return "無";
    }
    Item best = null;
    for (int i = 0; i < bag.length; i++) {
        if (bag[i] == null) {
            continue;
        }
        if (best == null || bag[i].stock < best.stock) {
            best = bag[i];
        }
    }
    return best == null ? "無" : best.name;
}

static String highestValueName(Item[] bag) {
    if (bag == null) {
        return "無";
    }
    Item best = null;
    for (int i = 0; i < bag.length; i++) {
        if (bag[i] == null) {
            continue;
        }
        if (best == null || bag[i].totalValue() > best.totalValue()) {
            best = bag[i];
        }
    }
    return best == null ? "無" : best.name;
}

static int countOutOfStock(Item[] bag) {
    if (bag == null) {
        return 0;
    }
    int count = 0;
    for (int i = 0; i < bag.length; i++) {
        if (bag[i] != null && bag[i].stock == 0) {
            count++;
        }
    }
    return count;
}
