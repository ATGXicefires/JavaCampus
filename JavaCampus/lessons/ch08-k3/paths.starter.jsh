class Path {
    String name;
    int weeklyHours;

    Path(String name, int weeklyHours) {
        this.name = (name == null || name.isBlank()) ? "未定" : name.trim();
        this.weeklyHours = weeklyHours < 0 ? 0 : weeklyHours;
    }

    boolean isSuitable(int enthusiasm) {
        return false;
    }

    String describe(int enthusiasm) {
        return name + "：還不知道";
    }

    static String recommend(Path[] paths, int enthusiasm) {
        if (paths == null) {
            return "先留在原地觀察";
        }
        for (Path p : paths) {
            if (p != null && p.isSuitable(enthusiasm)) {
                return p.describe(enthusiasm);
            }
        }
        return "先留在原地觀察";
    }
}

// 寫 DesignPath 與 CodePath 繼承 Path
// 各自在建構子接收 int weeklyHours 並透過 super(...) 交給父類別
// 用 @Override 覆寫 isSuitable(int enthusiasm) 與 describe(int enthusiasm)
