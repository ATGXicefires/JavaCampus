public class Main {
    public static void main(String[] args) {
        int today = 3;
        for (int i = 0; i < 10; i++) {
            int weekday = (today + i) % 7;
            if (weekday == 0 || weekday == 6) {
                continue;
            }
            System.out.println("還有 " + (10 - i) + " 天");
        }
    }

    static int schoolDays(int days, int startWeekday) {
        int n = 0;
        for (int i = 0; i < days; i++) {
            int weekday = (startWeekday + i) % 7;
            if (weekday == 0 || weekday == 6) {
                continue;
            }
            n++;
        }
        return n;
    }
}
