class MascotView implements ____ {
    String speaker;
    String message;
    boolean hasMessage;

    MascotView(String speaker, String message) {
        this.speaker = (speaker == null || speaker.trim().isEmpty()) ? "滷肉" : speaker.trim();
        this.hasMessage = message != null && !message.trim().isEmpty();
        this.message = hasMessage ? message.trim() : "…";
    }

    public boolean isReady() {
        return ____;
    }

    public String render(int maxWidth) {
        if (maxWidth <= 0) {
            return "";
        }
        ____
    }
}

class Panel {
    Drawable[] items;
    int width;

    Panel(Drawable[] items, int width) {
        this.items = items == null ? new Drawable[0] : items;
        this.width = Math.max(0, width);
    }

    int readyCount() {
        ____
    }

    String renderAll() {
        ____
    }
}
