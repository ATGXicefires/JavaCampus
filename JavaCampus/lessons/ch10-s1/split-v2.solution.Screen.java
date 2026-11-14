public class Screen {
    private String title;
    private boolean unlocked;
    private String filter;
    private int clicks;

    public Screen(String title, boolean unlocked) {
        this.title = (title == null || title.isBlank()) ? "未命名" : title.trim();
        this.unlocked = unlocked;
        this.filter = "全部";
        this.clicks = 0;
    }

    public String getTitle() {
        return title;
    }

    public String getFilter() {
        return filter;
    }

    public int getClicks() {
        return clicks;
    }

    public void unlock() {
        this.unlocked = true;
    }

    public void setFilter(String filter) {
        this.filter = (filter == null || filter.isBlank()) ? "全部" : filter.trim();
    }

    public boolean select() {
        if (!unlocked) {
            return false;
        }
        clicks++;
        return true;
    }

    public String status() {
        String state = unlocked ? "可用" : "鎖定";
        return title + " [" + state + "] 篩選:" + filter + " 點擊:" + clicks;
    }
}
