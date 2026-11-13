class Plan {
    String title;
    int weeklyHours;

    Plan(String title, int weeklyHours) {
        this.title = (title == null || title.isBlank()) ? "未命名計畫" : title.trim();
        this.weeklyHours = weeklyHours < 0 ? 0 : weeklyHours;
    }

    int monthlyHours() {
        return weeklyHours * 4;
    }

    String summary() {
        return title + "（每週 " + weeklyHours + " 小時）";
    }

    String hobbyNote() {
        return "畫畫當興趣就好";
    }

    static int totalMonthlyHours(Plan[] plans) {
        if (plans == null) {
            return 0;
        }
        int total = 0;
        for (Plan p : plans) {
            if (p != null) {
                total += p.monthlyHours();
            }
        }
        return total;
    }
}

class MyPlan extends ____ {
    String dream;

    MyPlan(String title, int weeklyHours, String dream) {
        super(____);
        this.dream = (dream == null || dream.isBlank()) ? "繼續畫畫" : dream.trim();
    }
}
