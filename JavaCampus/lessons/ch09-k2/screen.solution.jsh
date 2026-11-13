interface Drawable {
    String render(int maxWidth);
    boolean isReady();
}

class Button implements Drawable {
    String text;
    boolean enabled;

    Button(String label, boolean enabled) {
        this.text = label == null ? "" : label.trim();
        this.enabled = enabled;
    }

    public boolean isReady() {
        return enabled && !text.isEmpty();
    }

    public String render(int maxWidth) {
        if (maxWidth < 5) {
            return "";
        }
        String base = text.isEmpty() ? "按鈕" : text;
        String display = enabled ? base : base + "(停用)";
        int cap = maxWidth - 4;
        if (display.length() > cap) {
            display = cap >= 2 ? display.substring(0, cap - 1) + "…" : display.substring(0, cap);
        }
        return "[ " + display + " ]";
    }
}

class ProgressBar implements Drawable {
    int done;
    int total;

    ProgressBar(int done, int total) {
        if (total <= 0) {
            this.done = 0;
            this.total = 0;
        } else {
            this.total = total;
            if (done < 0) {
                this.done = 0;
            } else if (done > total) {
                this.done = total;
            } else {
                this.done = done;
            }
        }
    }

    public boolean isReady() {
        return total > 0 && done == total;
    }

    public String render(int maxWidth) {
        if (maxWidth <= 0) {
            return "";
        }
        int pct = total <= 0 ? 0 : (done * 100) / total;
        String pctText = pct + "%";
        String suffix = " " + pctText;
        int barWidth = maxWidth - suffix.length();
        if (barWidth <= 0) {
            return maxWidth >= pctText.length() ? pctText : "";
        }
        int filled = total <= 0 ? 0 : (done * barWidth) / total;
        int empty = barWidth - filled;
        return "■".repeat(filled) + "□".repeat(empty) + suffix;
    }
}

class MascotView implements Drawable {
    String speaker;
    String message;
    boolean hasMessage;

    MascotView(String speaker, String message) {
        this.speaker = (speaker == null || speaker.trim().isEmpty()) ? "滷肉" : speaker.trim();
        this.hasMessage = message != null && !message.trim().isEmpty();
        this.message = hasMessage ? message.trim() : "…";
    }

    public boolean isReady() {
        return hasMessage;
    }

    public String render(int maxWidth) {
        if (maxWidth <= 0) {
            return "";
        }
        String full = speaker + "：「" + message + "」";
        if (full.length() <= maxWidth) {
            return full;
        }
        return maxWidth >= 2 ? full.substring(0, maxWidth - 1) + "…" : full.substring(0, 1);
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
        int count = 0;
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].isReady()) {
                count++;
            }
        }
        return count;
    }

    String renderAll() {
        String out = "";
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                continue;
            }
            String line = items[i].render(width);
            if (!line.isEmpty()) {
                if (!out.isEmpty()) {
                    out += "\n";
                }
                out += line;
            }
        }
        return out;
    }
}
