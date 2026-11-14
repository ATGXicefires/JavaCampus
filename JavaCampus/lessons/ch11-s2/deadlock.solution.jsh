public class Main {
    public static void main(String[] args) {
        int days = daysUntilBudgetReady(100, 50, 300, 14);
        System.out.println("需要 " + days + " 天");
    }

    public static int daysUntilBudgetReady(int saved, int dailySave, int target, int maxDays) {
        if (saved >= target) {
            return 0;
        }
        if (dailySave <= 0 || maxDays <= 0) {
            return -1;
        }
        int day = 0;
        while (true) {
            day++;
            if (day % 7 != 6 && day % 7 != 0) {
                saved += dailySave;
                if (saved >= target) {
                    break;
                }
            }
            if (day >= maxDays) {
                return -1;
            }
        }
        return day;
    }
}
