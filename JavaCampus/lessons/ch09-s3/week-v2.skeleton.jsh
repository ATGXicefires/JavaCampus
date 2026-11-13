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
        ____
    }

    int totalCalories() {
        ____
    }

    int vegDays() {
        ____
    }

    boolean isOverBudget() {
        return ____;
    }

    String summary() {
        ____
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
