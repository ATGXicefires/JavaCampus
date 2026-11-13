public class Memory {
    public static final String FIRST_LINE = "學長，你在說外星語嗎？";
    public static final String SECRET_CMD = "HELP_SENIOR";

    public String firstLine() {
        return FIRST_LINE;
    }

    public boolean isFirstLine(String input) {
        if (input == null) {
            return false;
        }
        return FIRST_LINE.equals(input.trim());
    }
}