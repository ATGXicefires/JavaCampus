public class Main {
    public static void main(String[] args) {
        printCountdown(10, 3, new int[]{1, 6});
    }

    public static boolean isOffDay(int weekday, int dayIndex, int[] holidays) {
        int w = ((weekday % 7) + 7) % 7;
        if (w == 0 || w == 6) {
            return true;
        }
        if (holidays != null) {
            for (int h : holidays) {
                if (h == dayIndex) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void printCountdown(int totalDays, int startWeekday, int[] holidays) {
        for (int i = 0; i < totalDays; i++) {
            int weekday = ((startWeekday + i) % 7 + 7) % 7;
            if (isOffDay(weekday, i, holidays)) {
                continue;
            }
            System.out.println("距離畢業還有 " + (totalDays - i) + " 天");
        }
    }
}
