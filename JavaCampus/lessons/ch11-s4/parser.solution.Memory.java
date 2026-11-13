public class Memory {
    public static final String FIRST_LINE = "學長，你在說外星語嗎？";
    public static final String SECRET_CMD = "HELP_SENIOR";
    public static final String EASTER_REPLY = "彩蛋解鎖：第一年還請多多指教！";
    public static final String HELP_REPLY = "可用指令：HELP_SENIOR / PING / 學長，你在說外星語嗎？";

    public String firstLine() {
        return FIRST_LINE;
    }

    public boolean isFirstLine(String input) {
        if (input == null) {
            return false;
        }
        return FIRST_LINE.equals(input.trim());
    }

    public String parseCommand(String input) {
        if (input == null) {
            return "";
        }
        String cmd = input.trim();
        if (cmd.startsWith("/")) {
            cmd = cmd.substring(1).trim();
        }
        if (cmd.isEmpty()) {
            return "";
        }
        if (cmd.equals(FIRST_LINE)) {
            return EASTER_REPLY;
        }
        if (cmd.equalsIgnoreCase(SECRET_CMD)) {
            return HELP_REPLY;
        }
        if (cmd.equalsIgnoreCase("PING")) {
            return "PONG";
        }
        return "";
    }
}