class TitleBanner implements ____ {
    String title;
    boolean hasTitle;

    TitleBanner(String title) {
        this.hasTitle = title != null && !title.trim().isEmpty();
        this.title = hasTitle ? title.trim() : "未命名";
    }

    public boolean isReady() {
        return ____;
    }

    public String render(int maxWidth) {
        ____
    }
}

// 在 Panel 類別裡加上 String summary() 方法
____
