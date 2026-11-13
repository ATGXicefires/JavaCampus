interface Drawable {
    ____ render(int maxWidth);
    boolean ____();
}

class Button implements ____ {
    String text;
    boolean enabled;

    Button(String label, boolean enabled) {
        this.text = ____;
        this.enabled = enabled;
    }

    public boolean isReady() {
        return ____;
    }

    public String render(int maxWidth) {
        if (maxWidth < 5) {
            return "";
        }
        ____
        return "[ " + display + " ]";
    }
}
