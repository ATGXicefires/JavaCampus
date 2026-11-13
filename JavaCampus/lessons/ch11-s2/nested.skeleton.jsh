public class Main {
    public static void main(String[] args) {
        int days = daysUntilBudgetReady(100, 50, 300, 14);
        System.out.println("需要 " + days + " 天");
        boolean[][] seats = {
            {true, false, true},
            {false, false, true}
        };
        System.out.println(findFirstEmptySeat(seats));
        System.out.println(findConsecutivePair(seats));
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

    public static String findFirstEmptySeat(boolean[][] seats) {
        if (seats == null) {
            return "已客滿";
        }
        String found = "已客滿";
        outer:
        for (int r = 0; r < seats.length; r++) {
            if (seats[r] == null) {
                continue;
            }
            for (int c = 0; c < seats[r].length; c++) {
                if (!seats[r][c]) {
                    found = "第 " + (r + 1) + " 排第 " + (c + 1) + " 號";
                    ____;
                }
            }
        }
        return found;
    }

    public static String findConsecutivePair(boolean[][] seats) {
        if (seats == null) {
            return "-1,-1";
        }
        String found = "-1,-1";
        outer:
        for (int r = 0; r < seats.length; r++) {
            if (seats[r] == null) {
                continue;
            }
            for (int c = 0; c < ____; c++) {
                if (!seats[r][c] && !seats[r][c + 1]) {
                    found = r + "," + c;
                    break outer;
                }
            }
        }
        return found;
    }
}
