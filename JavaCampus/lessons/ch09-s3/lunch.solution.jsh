interface Lunch {
    String name();
    int price();
    int calories();
    boolean isVegetarian();
}

class LuRouFan implements Lunch {
    boolean large;
    boolean addEgg;

    LuRouFan(boolean large, boolean addEgg) {
        this.large = large;
        this.addEgg = addEgg;
    }

    public String name() {
        return "滷肉飯" + (large ? "(大)" : "") + (addEgg ? "+蛋" : "");
    }

    public int price() {
        return 45 + (large ? 15 : 0) + (addEgg ? 15 : 0);
    }

    public int calories() {
        return 420 + (large ? 180 : 0) + (addEgg ? 80 : 0);
    }

    public boolean isVegetarian() {
        return false;
    }
}

class Bento implements Lunch {
    String mainDish;
    int basePrice;
    boolean extraVeg;

    Bento(String mainDish, int basePrice, boolean extraVeg) {
        this.mainDish = (mainDish == null || mainDish.trim().isEmpty()) ? "招牌" : mainDish.trim();
        this.basePrice = Math.max(0, basePrice);
        this.extraVeg = extraVeg;
    }

    public String name() {
        return mainDish + "便當" + (extraVeg ? "+青菜" : "");
    }

    public int price() {
        return basePrice + (extraVeg ? 20 : 0);
    }

    public int calories() {
        return 650 + (extraVeg ? 50 : 0);
    }

    public boolean isVegetarian() {
        return mainDish.contains("素");
    }
}

class SaladBowl implements Lunch {
    int count;

    SaladBowl(int toppings) {
        this.count = Math.max(0, toppings);
    }

    public String name() {
        return "沙拉碗(" + count + "配料)";
    }

    public int price() {
        int raw = 80 + count * 15;
        return count >= 3 ? (raw * 9) / 10 : raw;
    }

    public int calories() {
        return 220 + count * 60;
    }

    public boolean isVegetarian() {
        return true;
    }
}
