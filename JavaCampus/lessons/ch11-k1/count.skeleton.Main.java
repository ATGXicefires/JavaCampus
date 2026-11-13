public class Main {
    public static void main(String[] args) {
        String[] log = ChatLog.load();
        System.out.println(count(log, "學長") + " 次");
    }

    public static int count(String[] log, String word) {
        if (____) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < log.length; i++) {
            if (____) {
                total++;
            }
        }
        return total;
    }

    public static int countWhile(String[] log, String word) {
        if (____) {
            return 0;
        }
        int total = 0;
        int i = 0;
        while (i < log.length) {
            if (____) {
                total++;
            }
            ____;
        }
        return total;
    }
}
