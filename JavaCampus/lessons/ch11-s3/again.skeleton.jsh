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
        } while (____);
        System.out.println("第 " + round + " 次答對了！");
    }
}
