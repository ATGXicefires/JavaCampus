public class Main {
    public static void main(String[] args) {
        int[] holidays = {1, 6};
        printCountdown(10, 3, holidays);
        System.out.println("實際上課日：" + countSchoolDays(0, 9, 3, holidays) + " 天");
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

    public static int countSchoolDays(int startDay, int endDay, int dayZeroWeekday, int[] holidays) {
        if (startDay > endDay) {
            return 0;
        }
        int count = 0;
        for (int d = startDay; d <= endDay; d++) {
            int weekday = ((dayZeroWeekday + d) % 7 + 7) % 7;
            if (isOffDay(weekday, d, holidays)) {
                continue;
            }
            count++;
        }
        return count;
    }
}
