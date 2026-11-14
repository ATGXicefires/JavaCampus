public class Main {
    public static void main(String[] args) {
        // today 是星期三 (3)，總共倒數 10 天，第 1 天與第 6 天是國定假日
        int today = 3;
        for (int i = 0; i < 10; i++) {
            System.out.println("距離畢業還有 " + (10 - i) + " 天");
        }
    }

    // 實作 isOffDay 與 printCountdown，並改寫 main 呼叫 printCountdown
}
