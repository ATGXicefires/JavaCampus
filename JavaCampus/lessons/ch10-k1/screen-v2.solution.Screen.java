public class Screen {
    private String title;
    private boolean unlocked;
    private int visitCount;

    public Screen(String title, boolean unlocked) {
        this.title = (title == null || title.isBlank()) ? "未命名" : title.trim();
        this.unlocked = unlocked;
        this.visitCount = 0;
    }

    public String getTitle() {
        return title;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void unlock() {
        this.unlocked = true;
    }

    public boolean open() {
        if (!unlocked) {
            return false;
        }
        visitCount++;
        return true;
    }

    public String render() {
        if (unlocked) {
            return "== " + title + " (點擊:" + visitCount + ") ==";
        }
        return "== " + title + " [未解鎖] ==";
    }
}
