class Sketch {
    String month;
    String title;

    Sketch(String month, String title) {
        this.month = (month == null || month.isBlank()) ? "未知" : month.trim();
        this.title = (title == null || title.isBlank()) ? "未命名" : title.trim();
    }

    int academicOrder() {
        String num = month.replace("月", "").trim();
        int m;
        try {
            m = Integer.parseInt(num);
        } catch (NumberFormatException e) {
            return 99;
        }
        if (m < 1 || m > 12) {
            return 99;
        }
        return m >= 9 ? m - 8 : m + 4;
    }

    @Override
    public String toString() {
        return month + "：" + title;
    }
}
