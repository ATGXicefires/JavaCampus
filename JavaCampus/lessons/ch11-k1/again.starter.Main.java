public class Main {
    public static void main(String[] args) {
        String[] log = ChatLog.load();
        String[] answers = {"好", "好", "不要"};
        // 用 do-while：至少統計一次，回答是「好」就再來
        // 每次印 第 1 次：478 次 這樣的一行
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
