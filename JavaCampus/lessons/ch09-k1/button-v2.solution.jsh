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
