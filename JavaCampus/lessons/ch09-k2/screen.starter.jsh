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

// 1. 實作 class MascotView implements Drawable
// 2. 實作 class Panel，包含 readyCount() 與 renderAll()
