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

class DesignPath extends Path {
    DesignPath(int weeklyHours) {
        super("廣設科", weeklyHours);
    }

    @Override
    boolean isSuitable(int enthusiasm) {
        return enthusiasm >= 60 && weeklyHours >= 5;
    }

    @Override
    String describe(int enthusiasm) {
        return name + "：畫我喜歡的東西（熱情 " + enthusiasm + "%）";
    }
}

class CodePath extends Path {
    CodePath(int weeklyHours) {
        super("資訊科", weeklyHours);
    }

    @Override
    boolean isSuitable(int enthusiasm) {
        return enthusiasm >= 70 && weeklyHours >= 8;
    }

    @Override
    String describe(int enthusiasm) {
        return name + "：把我畫的東西變成會動的（每週 " + weeklyHours + " 小時）";
    }
}

class StayPath extends Path {
    String name;

    StayPath(int weeklyHours) {
        super("留下來", weeklyHours);
    }

    boolean isSuitable(double enthusiasm) {
        return enthusiasm >= 50;
    }

    String descripe(int enthusiasm) {
        return name + "：先試試看（每週 " + weeklyHours + " 小時）";
    }
}
