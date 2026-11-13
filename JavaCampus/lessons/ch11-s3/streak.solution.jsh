public class Main {
    public static void main(String[] args) {
        String[] tries = {"吃控肉飯", "吃 滷肉飯", "吃滷肉飯"};
        String answer = "吃滷肉飯";
        printPracticeLog(tries, answer, 5);
        System.out.println("本次得分：" + practiceScore(tries, answer, 5, 100, 10) + " 分");
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

    public static int practiceScore(String[] tries, String answer, int maxTries, int baseScore, int penalty) {
        int k = countAttempts(tries, answer, maxTries);
        if (k <= 0) {
            return 0;
        }
        return Math.max(0, baseScore - (k - 1) * penalty);
    }

    public static int totalPassedSessions(String[][] sessions, String answer, int maxTries) {
        if (sessions == null) {
            return 0;
        }
        int passed = 0;
        for (String[] session : sessions) {
            if (session != null && countAttempts(session, answer, maxTries) > 0) {
                passed++;
            }
        }
        return passed;
    }
}
