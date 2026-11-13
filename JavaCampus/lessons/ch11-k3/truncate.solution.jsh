public class Main {
    public static void main(String[] args) {
        String[] drafts = {
            "這一年每次程式寫不出來都是你坐在旁邊陪我除錯到最後一刻。",
            "謝謝你把外星語翻成人話。",
            "---END---",
            "謝啦"
        };
        System.out.println(firstFit(drafts, 15));
        System.out.println(clipSentence("謝謝你把外星語翻成人話。後面還有很多感想寫不下。", 20));
        System.out.println(clipSentence("謝謝你一直陪我們 debug 到深夜！", 10));
    }

    public static String firstFit(String[] drafts, int maxLen) {
        if (drafts == null || maxLen <= 0) {
            return "（空白）";
        }
        String result = "（空白）";
        for (String d : drafts) {
            if (d == null) {
                continue;
            }
            String s = d.trim();
            if (s.isEmpty()) {
                continue;
            }
            if (s.equals("---END---")) {
                break;
            }
            if (s.length() <= maxLen) {
                result = s;
                break;
            }
        }
        return result;
    }

    public static int countUntilKeyword(String[] paragraphs, String keyword) {
        if (paragraphs == null) {
            return 0;
        }
        int count = 0;
        for (String p : paragraphs) {
            if (p == null) {
                continue;
            }
            if (keyword != null && !keyword.isEmpty() && p.contains(keyword)) {
                break;
            }
            count++;
        }
        return count;
    }

    public static String clipSentence(String text, int maxLen) {
        if (text == null || maxLen <= 0) {
            return "";
        }
        String s = text.trim();
        if (s.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '。' || ch == '.') {
                out.append(ch);
                break;
            }
            if (i == maxLen - 1 && i + 1 < s.length()) {
                if (maxLen >= 2) {
                    return s.substring(0, maxLen - 1) + "…";
                }
                return s.substring(0, 1);
            }
            out.append(ch);
            if (i == maxLen - 1) {
                break;
            }
        }
        return out.toString();
    }
}
