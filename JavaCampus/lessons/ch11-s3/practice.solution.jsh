public class Main {
    public static void main(String[] args) {
        String[] tries = {"吃控肉飯", "吃 滷肉飯", "吃滷肉飯"};
        String answer = "吃滷肉飯";
        printPracticeLog(tries, answer, 5);
    }

    public static int countAttempts(String[] tries, String answer, int maxTries) {
        if (tries == null || tries.length == 0 || answer == null || maxTries <= 0) {
            return -1;
        }
        String target = answer.trim();
        int round = 0;
        do {
            round++;
            String attempt = tries[round - 1];
            if (attempt != null && attempt.trim().equals(target)) {
                return round;
            }
        } while (round < maxTries && round < tries.length);
        return -1;
    }

    public static void printPracticeLog(String[] tries, String answer, int maxTries) {
        if (tries == null || tries.length == 0 || answer == null || maxTries <= 0) {
            System.out.println("無法開始練習");
            return;
        }
        String target = answer.trim();
        int round = 0;
        do {
            round++;
            String attempt = tries[round - 1];
            if (attempt != null && attempt.trim().equals(target)) {
                System.out.println("第 " + round + " 次答對了！");
                return;
            }
            System.out.println("第 " + round + " 次：再試一次");
        } while (round < maxTries && round < tries.length);
        if (round >= maxTries) {
            System.out.println("超過上限，先休息一下");
        } else {
            System.out.println("題庫已用完");
        }
    }
}
