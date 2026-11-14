public class Badge implements Drawable {
    public String render(int score, int chapters) {
        String title;
        if (score >= 90) {
            title = "金徽章";
        } else if (____) {
            title = "銀徽章";
        } else {
            title = "繼續加油";
        }
        int safeChapters = ____;
        return "[" + title + "] 完成 " + safeChapters + " 章";
    }

    public String renderSummary(int bonus, int level) {
        return "等級：" + (bonus + level);
    }
}
