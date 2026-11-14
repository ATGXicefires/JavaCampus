class Menu {
    static final int LU_ROU_FAN = 35;
    static final int MILKFISH_SOUP = 60;
    static final int NEW_YEAR_SET = 880;

    static int calcOrder(int riceQty, int soupQty, int setQty) {
        if (riceQty < 0 || soupQty < 0 || setQty < 0) {
            return -1;
        }
        int total = riceQty * LU_ROU_FAN + soupQty * MILKFISH_SOUP + setQty * NEW_YEAR_SET;
        if (total >= 1000) {
            return total * 9 / 10;
        }
        return total;
    }
}

class Dish {
    private final String name;
    private final int basePrice;
    private int extra;

    Dish(String name, int basePrice) {
        // 防呆：空白名稱設 "無名菜色"；basePrice <= 0 設為 1
        this.name = name;
        this.basePrice = basePrice;
    }

    boolean adjustExtra(int delta) {
        // 錯誤示範：試圖修改 final 欄位，請改成檢查 0..20% 上限後記在 extra
        basePrice += delta;
        return true;
    }

    int currentPrice() {
        return basePrice + extra;
    }

    int getBasePrice() {
        return basePrice;
    }

    String getName() {
        return name;
    }
}

System.out.println("滷肉飯 " + Menu.LU_ROU_FAN + " 元");
System.out.println("虱目魚湯 " + Menu.MILKFISH_SOUP + " 元");
System.out.println("年菜套餐 " + Menu.NEW_YEAR_SET + " 元");

// 過年漲 100 元
Menu.NEW_YEAR_SET = 980;
