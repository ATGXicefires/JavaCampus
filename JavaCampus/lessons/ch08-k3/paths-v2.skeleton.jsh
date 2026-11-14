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
        super(____);
    }

    @Override
    boolean isSuitable(int enthusiasm) {
        return ____;
    }

    @Override
    String describe(int enthusiasm) {
        return ____;
    }
}

class CodePath extends Path {
    CodePath(int weeklyHours) {
        super(____);
    }

    @Override
    boolean isSuitable(int enthusiasm) {
        return ____;
    }

    @Override
    String describe(int enthusiasm) {
        return ____;
    }
}
