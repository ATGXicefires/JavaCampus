public class Main {
    public static void main(String[] args) {
        // 今天是星期三（3）。0 是星期日，6 是星期六，週末用 continue 跳過不印
        int today = 3;
        for (int i = 0; i < 10; i++) {
            System.out.println("還有 " + (10 - i) + " 天");
        }
    }
}
