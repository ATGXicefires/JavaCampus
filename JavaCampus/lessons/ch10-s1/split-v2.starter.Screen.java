public class Screen {
    private String title;
    private boolean unlocked;
    private String filter;
    private int clicks;

    public Screen(String title, boolean unlocked) {
        // 空或 null 標題改為 "未命名"，預設 filter 為 "全部"
        this.title = title;
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
        // 請補上解鎖邏輯
    }

    public void setFilter(String filter) {
        // 若 filter 為 null 或空白改回 "全部"，否則 trim()
    }

    public boolean select() {
        // 未解鎖時不可累加 clicks，應回傳 false
        clicks++;
        return true;
    }

    public String status() {
        // 格式：<標題> [<可用或鎖定>] 篩選:<filter> 點擊:<clicks>
        return "";
    }
}
