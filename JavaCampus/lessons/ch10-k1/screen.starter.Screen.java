public class Screen {
    private String title;
    private boolean unlocked;
    private int visitCount;

    public Screen(String title, boolean unlocked) {
        // 若 title 為 null 或空白，設為 "未命名"，否則 trim()
        this.title = title;
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
        // 請補上解鎖邏輯
    }

    public boolean open() {
        // 僅在已解鎖時遞增 visitCount 並回傳 true
        return false;
    }

    public String render() {
        // 依 unlocked 狀態回傳對應標題格式
        return "";
    }
}
