static int total(int[] counts) {
    int sum = 0;
    for (int i = 0; i < counts.length; i++) {
        if (counts[i] > 0) {
            sum += counts[i];
        }
    }
    return sum;
}

static int percent(int part, int whole) {
    if (whole <= 0 || part <= 0) {
        return 0;
    }
    if (part >= whole) {
        return 100;
    }
    return part * 100 / whole;
}

static void report(String label, int[] doneList, int whole) {
    String name = label.trim();
    if (name.isEmpty()) {
        name = "未分類";
    }
    int part = ____(doneList);
    int p = ____(part, whole);
    String status;
    if (p == 100) {
        status = "[已完成]";
    } else if (p >= 50) {
        status = "[進行中]";
    } else {
        status = "[剛起步]";
    }
    System.out.println(name + " " + status + " " + part + "/" + whole + " (" + p + "%)");
}

report("方法篇", new int[]{1, 2}, 4);
report("____", new int[]{5, 5}, 8);
