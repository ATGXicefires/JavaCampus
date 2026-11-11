public class Main {
    public static void main(String[] args) {
        // today 是今天星期幾：0 是星期日，6 是星期六，3 是星期三
        // 在迴圈裡算出每一圈是星期幾，遇到星期六、日就用 continue 跳過不印
        int today = 3;
        for (int i = 0; i < 10; i++) {
            System.out.println("還有 " + (10 - i) + " 天");
        }
    }
}
