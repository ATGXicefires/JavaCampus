static boolean showBadge(int done, int total, int score) {
    return total > 0 && done == total && score >= 60 && score <= 100;
}

static boolean showHint(int tries, boolean passed, boolean practice) {
    return !passed && (tries >= 2 || practice);
}

static void printGrid(int rows, int cols, int done) {
    if (rows <= 0 ____ cols <= 0) {
        return;
    }
    int count = 0;
    for (int r = 0; r < rows; r++) {
        String line = "";
        for (int c = 0; c < cols; c++) {
            count++;
            if (count <= ____) {
                line += "■";
            } else {
                line += "□";
            }
        }
        System.out.println(line);
    }
}

printGrid(rows, cols, done);
