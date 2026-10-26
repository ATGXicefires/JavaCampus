public class Main {
    public static void main(String[] args) {
    }

    static int countName(String[] log, String name) {
        int n = 0;
        for (String line : log) {
            if (line.startsWith("（已收回）")) {
                continue;
            }
            if (line.contains(name)) {
                n++;
            }
        }
        return n;
    }
}
