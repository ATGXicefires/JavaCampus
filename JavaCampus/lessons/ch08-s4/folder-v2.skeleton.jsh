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

class Folder {
    String name;
    Sketch[] items;

    Folder(String name, Sketch[] items) {
        this.name = (name == null || name.isBlank()) ? "未命名資料夾" : name.trim();
        if (items == null) {
            this.items = new Sketch[0];
        } else {
            int count = 0;
            for (Sketch s : items) {
                if (s != null) {
                    count++;
                }
            }
            this.items = new Sketch[count];
            int idx = 0;
            for (Sketch s : items) {
                if (s != null) {
                    this.items[idx++] = s;
                }
            }
        }
    }

    int size() {
        return items.length;
    }

    Folder sortedByAcademicOrder() {
        Sketch[] copy = new Sketch[items.length];
        for (int i = 0; i < items.length; i++) {
            copy[i] = items[i];
        }
        for (int i = 0; i < copy.length - 1; i++) {
            for (int j = 0; j < copy.length - 1 - i; j++) {
                if (copy[j].academicOrder() > copy[j + 1].academicOrder()) {
                    Sketch tmp = copy[j];
                    copy[j] = copy[j + 1];
                    copy[j + 1] = tmp;
                }
            }
        }
        return ____;
    }

    @Override
    public String toString() {
        if (items.length == 0) {
            return name + "（0 張）：資料夾是空的";
        }
        String result = name + "（共 " + items.length + " 張，首張：" + items[0].month + "，末張：" + items[items.length - 1].month + "）";
        for (int i = 0; i < items.length; i++) {
            result += "\n  " + (i + 1) + ". " + ____;
        }
        return result;
    }
}

int len = Math.min(months.length, titles.length);
Sketch[] sketches = new Sketch[len];
for (int i = 0; i < len; i++) {
    sketches[i] = new Sketch(months[i], titles[i]);
}
Folder folder = new Folder("亭瑄的介面", sketches);
System.out.println(folder.sortedByAcademicOrder());
