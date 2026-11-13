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

class PortfolioPage {
    Section[] included;
    int linesUsed;

    PortfolioPage(Section[] rawSections, int maxLines) {
        int limit = Math.max(0, maxLines);
        int validCount = 0;
        if (rawSections != null) {
            for (int i = 0; i < rawSections.length; i++) {
                if (rawSections[i] != null && !rawSections[i].isEmpty()) {
                    validCount++;
                }
            }
        }
        Section[] valid = new Section[validCount];
        int idx = 0;
        if (rawSections != null) {
            for (int i = 0; i < rawSections.length; i++) {
                if (rawSections[i] != null && !rawSections[i].isEmpty()) {
                    valid[idx++] = rawSections[i];
                }
            }
        }
        // 穩定排序（插入排序）
        for (int i = 1; i < valid.length; i++) {
            Section key = valid[i];
            int j = i - 1;
            while (j >= 0 && valid[j].priority() > key.priority()) {
                valid[j + 1] = valid[j];
                j--;
            }
            valid[j + 1] = key;
        }
        // 依行數預算篩選
        Section[] temp = new Section[valid.length];
        int picked = 0;
        int used = 0;
        for (int i = 0; i < valid.length; i++) {
            int lines = valid[i].lineCount();
            if (used + lines <= limit) {
                temp[picked++] = valid[i];
                used += lines;
            }
        }
        this.included = new Section[picked];
        for (int i = 0; i < picked; i++) {
            this.included[i] = temp[i];
        }
        this.linesUsed = used;
    }

    int includedCount() {
        return included.length;
    }

    int usedLines() {
        return linesUsed;
    }

    String render() {
        String out = "";
        for (int i = 0; i < included.length; i++) {
            if (i > 0) {
                out += "\n---\n";
            }
            out += included[i].format();
        }
        return out;
    }
}
