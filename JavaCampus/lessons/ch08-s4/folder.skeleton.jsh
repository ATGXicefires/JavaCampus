class Sketch {
    String month;
    String title;

    Sketch(String month, String title) {
        this.month = month;
        this.title = title;
    }

    @Override
    public String toString() {
        return month + "：" + title;
    }
}

for (int i = 0; i < months.length; i++) {
    Sketch sketch = new Sketch(____, ____);
    System.out.println(sketch);
}
