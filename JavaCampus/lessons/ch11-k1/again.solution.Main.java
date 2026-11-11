public class Main {
    public static void main(String[] args) {
        String[] log = ChatLog.load();
        String[] answers = {"好", "好", "不要"};
        int round = 0;
        String answer;
        do {
            round++;
            System.out.println("第 " + round + " 次：" + count(log, "學長") + " 次");
            answer = answers[round - 1];
        } while (answer.equals("好"));
    }

    static int count(String[] log, String word) {
        int n = 0;
        for (int i = 0; i < log.length; i++) {
            if (log[i].contains(word)) {
                n++;
            }
        }
        return n;
    }

    static int countWhile(String[] log, String word) {
        int n = 0;
        int i = 0;
        while (i < log.length) {
            if (log[i].contains(word)) {
                n++;
            }
            i++;
        }
        return n;
    }
}
