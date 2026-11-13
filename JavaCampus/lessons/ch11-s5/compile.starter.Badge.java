public class Badge implements Drawable {
    String render(int score, int chapters) {
        String title;
        if (score >= 90) {
            title = "金徽章";
        } else if (score > 60) {
            title = "銀徽章";
        } else {
            title = "繼續加油";
        }
        return "[" + title + "] 完成 " + chapters + " 章";
    }

    public String renderSummary(int bonus, int level) {
        return "等級：" + bonus + level;
    }
}
