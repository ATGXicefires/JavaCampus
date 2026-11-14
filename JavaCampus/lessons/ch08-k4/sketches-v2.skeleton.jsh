class Sketch {
    String month;
    String title;

    Sketch(String month, String title) {
        this.month = (month == null || month.isBlank()) ? "未定" : month.trim();
        this.title = (title == null || title.isBlank()) ? "未命名草圖" : title.trim();
    }

    @Override
    public String toString() {
        return ____;
    }
}

class Folder {
    String name;
    Sketch[] items;

    Folder(String name, Sketch[] items) {
        this.name = (name == null || name.isBlank()) ? "草圖資料夾" : name.trim();
        this.items = items == null ? new Sketch[0] : items;
    }

    int countByMonth(String targetMonth) {
        if (targetMonth == null) {
            return 0;
        }
        String target = targetMonth.trim();
        int count = 0;
        for (Sketch s : items) {
            if (s != null && target.equals(s.month)) {
                count++;
            }
        }
        return count;
    }

    Folder filterMonth(String targetMonth) {
        String key = targetMonth == null ? "" : targetMonth.trim();
        int count = countByMonth(targetMonth);
        Sketch[] filtered = new Sketch[count];
        int idx = 0;
        for (Sketch s : items) {
            if (s != null && key.equals(s.month)) {
                filtered[idx++] = s;
            }
        }
        return new Folder(name + "[" + key + "]", filtered);
    }

    @Override
    public String toString() {
        int count = 0;
        for (Sketch s : items) {
            if (s != null) {
                count++;
            }
        }
        if (count == 0) {
            return name + "（0 張）：（空）";
        }
        String result = name + "（共 " + count + " 張）";
        for (Sketch s : items) {
            if (s != null) {
                result += "\n  - " + ____;
            }
        }
        return result;
    }
}

int len = Math.min(months.length, titles.length);
Sketch[] sketches = new Sketch[len];
for (int i = 0; i < len; i++) {
    sketches[i] = new Sketch(months[i], titles[i]);
}
Folder folder = new Folder("亭瑄的草圖", sketches);
System.out.println(folder);
