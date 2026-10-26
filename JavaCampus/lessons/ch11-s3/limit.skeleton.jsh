public class Main {
    public static void main(String[] args) {
        String[] tries = {"吃控肉飯", "吃 滷肉飯", "吃滷肉飯"};
        String answer = "吃滷肉飯";
        int round = 0;
        boolean right;
        do {
            right = tries[round].trim().equals(answer);
            round++;
            if (!right) {
                System.out.println("第 " + round + " 次：再試一次");
            }
        } while (!right);
        System.out.println("第 " + round + " 次答對了！");
    }

    static int triesNeeded(String[] tries, String answer, int limit) {
        for (int i = 0; i < tries.length && i < ____; i++) {
            if (tries[i].trim().equals(answer)) {
                return i + 1;
            }
        }
        return -1;
    }
}
