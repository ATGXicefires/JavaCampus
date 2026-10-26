public class Main {
    public static void main(String[] args) {
        System.out.println(Main.easterEgg("學長，你在說外星語嗎？"));
    }

    static String easterEgg(String input) {
        if (input.trim().equals(Memory.FIRST_LINE)) {
            return "梁亭瑄，我不太會說話。\n"
                    + "但是和你 pair programming 的這一年，\n"
                    + "是我人生最少 bug 的時期。\n"
                    + "如果可以的話，我想跟你繼續 commit 下去。\n"
                    + "// 這段程式碼沒有 expiry date";
        }
        return "";
    }
}
