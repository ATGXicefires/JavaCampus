public class Main {
    public static void main(String[] args) {
        String[] log = {"二弘早", "（已收回）二弘", "[系統] 二弘加入群組", "   ", "早安", "（已收回）學長", "二弘！"};
        System.out.println(summarize(log, "二弘"));
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

    public static String summarize(String[] log, String name) {
        String target = name == null ? "" : name;
        int valid = 0;
        int skipped = 0;
        int matched = 0;
        int chars = 0;
        if (log != null) {
            int i = 0;
            while (i < log.length) {
                String msg = log[i];
                ____;
                if (msg == null || msg.trim().isEmpty() || msg.startsWith("（已收回）") || msg.startsWith("[系統]")) {
                    skipped++;
                    continue;
                }
                valid++;
                chars += msg.trim().length();
                if (!target.isEmpty() && msg.contains(target)) {
                    matched++;
                }
            }
        }
        return ____;
    }
}
