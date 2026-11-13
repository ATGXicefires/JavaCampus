public class Main {
    public static void main(String[] args) {
        int days = daysUntilBudgetReady(100, 50, 300, 14);
        System.out.println("需要 " + days + " 天");
    }

    // 下面這段模擬有幾個狀態更新與邊界錯誤，請找出並修復
    public static int daysUntilBudgetReady(int saved, int dailySave, int target, int maxDays) {
        int day = 0;
        while (true) {
            if (day % 7 == 6 || day % 7 == 0) {
                continue;
            }
            day++;
            saved += dailySave;
            if (saved >= target) {
                break;
            }
        }
        return day;
    }
}
