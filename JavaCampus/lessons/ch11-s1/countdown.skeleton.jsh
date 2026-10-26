public class Main {
    public static void main(String[] args) {
        int today = 3;
        for (int i = 0; i < 10; i++) {
            int weekday = (today + i) % 7;
            if (weekday == 0 || weekday == 6) {
                ____;
            }
            System.out.println("還有 " + (10 - i) + " 天");
        }
    }
}
