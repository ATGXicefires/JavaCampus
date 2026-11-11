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
    Sketch sketch = new Sketch(months[i], titles[i]);
    System.out.println(sketch);
}
