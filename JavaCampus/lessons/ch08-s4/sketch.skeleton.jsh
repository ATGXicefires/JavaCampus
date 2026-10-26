class Sketch {
    String month;
    String title;

    Sketch(String month, String title) {
        this.month = month;
        this.title = title;
    }

    @Override
    public String ____() {
        return month + "：" + title;
    }
}
