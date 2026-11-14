class LuRouFan implements ____ {
    boolean large;
    boolean addEgg;

    LuRouFan(boolean large, boolean addEgg) {
        this.large = large;
        this.addEgg = addEgg;
    }

    public String name() {
        return ____;
    }

    public int price() {
        return ____;
    }

    public int calories() {
        return 420 + (large ? 180 : 0) + (addEgg ? 80 : 0);
    }

    public boolean isVegetarian() {
        return false;
    }
}

class Bento implements ____ {
    String mainDish;
    int basePrice;
    boolean extraVeg;

    Bento(String mainDish, int basePrice, boolean extraVeg) {
        this.mainDish = (mainDish == null || mainDish.trim().isEmpty()) ? "招牌" : mainDish.trim();
        this.basePrice = Math.max(0, basePrice);
        this.extraVeg = extraVeg;
    }

    public String name() {
        return ____;
    }

    public int price() {
        return basePrice + (extraVeg ? 20 : 0);
    }

    public int calories() {
        return 650 + (extraVeg ? 50 : 0);
    }

    public boolean isVegetarian() {
        return ____;
    }
}

class SaladBowl implements ____ {
    int count;

    SaladBowl(int toppings) {
        this.count = Math.max(0, toppings);
    }

    public String name() {
        return "沙拉碗(" + count + "配料)";
    }

    public int price() {
        ____
    }

    public int calories() {
        return 220 + count * 60;
    }

    public boolean isVegetarian() {
        return true;
    }
}
