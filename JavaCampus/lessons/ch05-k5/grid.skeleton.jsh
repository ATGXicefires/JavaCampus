static boolean showBadge(int done, int total, int score) {
    return done == total && score >= 60;
}

static boolean showHint(boolean triedTwice, boolean passed) {
    return triedTwice && !passed;
}

int count = 0;
for (int r = 0; r < rows; r++) {
    String line = "";
    for (int c = 0; c < ____; c++) {
        count++;
        if (count <= ____) {
            line += "■";
        } else {
            line += "□";
        }
    }
    System.out.println(line);
}
