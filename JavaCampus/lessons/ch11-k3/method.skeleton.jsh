public class Main {
    public static void main(String[] args) {
        String[] drafts = {
            "學長，這一年謝謝你教我寫程式，從 Hello World 一路寫到專題，每一行我都記得。",
            "學長，謝謝你每天陪我 debug，還有那些一起吃的滷肉飯。",
            "學長，畢業快樂，臺北很好，你一定可以的。",
            "謝謝你把外星語翻成人話。",
            "學長再見。"
        };
        for (String d : drafts) {
            if (d.length() <= 15) {
                System.out.println(d);
                break;
            }
        }
    }

    static String firstShort(String[] drafts, int max) {
        String found = "（空白）";
        for (String d : drafts) {
            if (d.length() <= max) {
                found = ____;
                break;
            }
        }
        return found;
    }
}
