public class Main {
    public static void main(String[] args) {
        String[] log = ChatLog.load();
        String[] answers = {"好", "好", "不要"};
        // 用 do-while：每次用 count 統計、印出 第 1 次：478 次 這樣的一行
        // 再看這次在 answers 裡的回答，是「好」就再來一次
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
