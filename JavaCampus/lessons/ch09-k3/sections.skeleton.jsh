class TextSection implements ____ {
    String title;
    String body;
    int priority;

    TextSection(String title, String body, int priority) {
        this.title = (title == null || title.trim().isEmpty()) ? "未命名" : title.trim();
        this.body = body == null ? "" : body.trim();
        this.priority = priority;
    }

    public String title() {
        return title;
    }

    public int priority() {
        return priority;
    }

    public boolean isEmpty() {
        return ____;
    }

    public int lineCount() {
        return ____;
    }

    public String format() {
        ____
    }
}

class BulletSection implements ____ {
    String title;
    String[] points;
    int priority;

    BulletSection(String title, String[] points, int priority) {
        this.title = (title == null || title.trim().isEmpty()) ? "未命名" : title.trim();
        this.priority = priority;
        ____
    }

    public String title() {
        return title;
    }

    public int priority() {
        return priority;
    }

    public boolean isEmpty() {
        return points.length == 0;
    }

    public int lineCount() {
        return points.length;
    }

    public String format() {
        ____
    }
}
