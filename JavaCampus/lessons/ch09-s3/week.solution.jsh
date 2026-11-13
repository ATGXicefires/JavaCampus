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

class WeekReport {
    String[] dayNames;
    Lunch[] meals;
    int budget;
    int n;

    WeekReport(String[] dayNames, Lunch[] meals, int budget) {
        this.dayNames = dayNames;
        this.meals = meals;
        this.budget = Math.max(0, budget);
        this.n = (dayNames == null || meals == null) ? 0 : Math.min(dayNames.length, meals.length);
    }

    int totalCost() {
        int sum = 0;
        for (int i = 0; i < n; i++) {
            if (meals[i] != null) {
                sum += meals[i].price();
            }
        }
        return sum;
    }

    int totalCalories() {
        int sum = 0;
        for (int i = 0; i < n; i++) {
            if (meals[i] != null) {
                sum += meals[i].calories();
            }
        }
        return sum;
    }

    int vegDays() {
        int count = 0;
        for (int i = 0; i < n; i++) {
            if (meals[i] != null && meals[i].isVegetarian()) {
                count++;
            }
        }
        return count;
    }

    boolean isOverBudget() {
        return totalCost() > budget;
    }

    String summary() {
        String out = "";
        for (int i = 0; i < n; i++) {
            String day = (dayNames[i] == null || dayNames[i].trim().isEmpty())
                    ? "第" + (i + 1) + "天"
                    : dayNames[i].trim();
            String line = meals[i] == null
                    ? day + "：未訂餐"
                    : day + "：" + meals[i].name() + " (" + meals[i].price() + "元)";
            if (!out.isEmpty()) {
                out += "\n";
            }
            out += line;
        }
        String footer = "總計：" + totalCost() + "元 / " + totalCalories() + "大卡 / 素食" + vegDays() + "天"
                + (isOverBudget() ? " [超預算]" : " [預算內]");
        return out.isEmpty() ? footer : out + "\n" + footer;
    }
}

Lunch[] menu = {
    new LuRouFan(true, true),
    new Bento("雞腿", 90, false),
    new SaladBowl(3),
    new Bento("養生素食", 85, true),
    new LuRouFan(false, false)
};
System.out.println(new WeekReport(days, menu, 400).summary());
