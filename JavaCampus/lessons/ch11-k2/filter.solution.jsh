public class Main {
    public static void main(String[] args) {
    }

    public static int countValidMessages(String[] log, String name) {
        if (log == null || name == null || name.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (String msg : log) {
            if (msg == null || msg.trim().isEmpty() || msg.startsWith("（已收回）") || msg.startsWith("[系統]")) {
                continue;
            }
            if (msg.contains(name)) {
                total++;
            }
        }
        return total;
    }

    public static int countValidChars(String[] log) {
        if (log == null) {
            return 0;
        }
        int chars = 0;
        for (String msg : log) {
            if (msg == null || msg.trim().isEmpty() || msg.startsWith("（已收回）") || msg.startsWith("[系統]")) {
                continue;
            }
            chars += msg.trim().length();
        }
        return chars;
    }
}
