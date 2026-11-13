interface Section {
    String title();
    String format();
    int priority();
    int lineCount();
    boolean isEmpty();
}

class TextSection implements Section {
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
        return body.isEmpty();
    }

    public int lineCount() {
        if (isEmpty()) {
            return 0;
        }
        return body.split("\n", -1).length;
    }

    public String format() {
        if (isEmpty()) {
            return "";
        }
        return "【" + title + "】\n" + body;
    }
}

class BulletSection implements Section {
    String title;
    String[] points;
    int priority;

    BulletSection(String title, String[] points, int priority) {
        this.title = (title == null || title.trim().isEmpty()) ? "未命名" : title.trim();
        this.priority = priority;
        if (points == null) {
            this.points = new String[0];
        } else {
            int count = 0;
            for (int i = 0; i < points.length; i++) {
                if (points[i] != null && !points[i].trim().isEmpty()) {
                    count++;
                }
            }
            this.points = new String[count];
            int idx = 0;
            for (int i = 0; i < points.length; i++) {
                if (points[i] != null && !points[i].trim().isEmpty()) {
                    this.points[idx++] = points[i].trim();
                }
            }
        }
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
        if (isEmpty()) {
            return "";
        }
        String out = "【" + title + "】";
        for (int i = 0; i < points.length; i++) {
            out += "\n" + (i + 1) + ". " + points[i];
        }
        return out;
    }
}
