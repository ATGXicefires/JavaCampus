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

System.out.println("滷肉飯 " + Menu.LU_ROU_FAN + " 元");
System.out.println("虱目魚湯 " + Menu.MILKFISH_SOUP + " 元");
System.out.println("年菜套餐 " + Menu.NEW_YEAR_SET + " 元");
