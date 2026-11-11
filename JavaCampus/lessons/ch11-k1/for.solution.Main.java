public class Main {
    public static void main(String[] args) {
        String[] log = ChatLog.load();
        System.out.println(count(log, "學長") + " 次");
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
}
