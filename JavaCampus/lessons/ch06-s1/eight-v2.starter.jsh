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

// 用 names 建立 Gacha[] box，填入扭蛋並印出每一行
// 再寫出 static int totalBoxValue(Gacha[] box) 計算非 null 扭蛋的總價值
