class Sketch {
    String month;
    String title;

    // 寫建構子 Sketch(String month, String title)（含 null/空白防呆與 trim）
    // 覆寫 public String toString()
}

class Folder {
    String name;
    Sketch[] items;

    // 寫建構子 Folder(String name, Sketch[] items)
    // 實作 countByMonth(String targetMonth)、filterMonth(String targetMonth)
    // 覆寫 public String toString()
}

// 依照 months 和 titles 建立 Sketch[]，放進 new Folder("亭瑄的草圖", sketches) 後印出
