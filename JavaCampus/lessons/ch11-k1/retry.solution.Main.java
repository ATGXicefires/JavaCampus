public class Main {
    public static void main(String[] args) {
        String[] log = ChatLog.load();
        runSummary(log, "學長", new String[]{"好", "好", "不要"}, 5);
    }

    public static int count(String[] log, String word) {
        if (log == null || word == null || word.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < log.length; i++) {
            if (log[i] != null && log[i].contains(word)) {
                total++;
            }
        }
        return total;
    }

    public static int countWhile(String[] log, String word) {
        if (log == null || word == null || word.isEmpty()) {
            return 0;
        }
        int total = 0;
        int i = 0;
        while (i < log.length) {
            if (log[i] != null && log[i].contains(word)) {
                total++;
            }
            i++;
        }
        return total;
    }

    public static int askRetry(String[] answers, int maxRounds) {
        if (maxRounds <= 0) {
            return 0;
        }
        int rounds = 0;
        boolean again;
        do {
            rounds++;
            again = false;
            if (answers != null && rounds - 1 < answers.length && answers[rounds - 1] != null) {
                if (answers[rounds - 1].trim().equals("好") && rounds < maxRounds) {
                    again = true;
                }
            }
        } while (again);
        return rounds;
    }

    public static void runSummary(String[] log, String word, String[] answers, int maxRounds) {
        if (maxRounds <= 0) {
            return;
        }
        int rounds = 0;
        boolean again;
        do {
            rounds++;
            System.out.println("第 " + rounds + " 次：" + count(log, word) + " 次");
            again = false;
            String ans = (answers != null && rounds - 1 < answers.length) ? answers[rounds - 1] : null;
            if (ans != null && ans.trim().equals("好")) {
                if (rounds < maxRounds) {
                    again = true;
                } else {
                    System.out.println("已達上限，停止重試");
                }
            }
        } while (again);
    }
}
