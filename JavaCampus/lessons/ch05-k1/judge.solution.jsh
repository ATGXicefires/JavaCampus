static boolean isPass(int score) {
    return score >= 60;
}

static boolean isValid(int score) {
    return score >= 0 && score <= 100;
}

static String judge(int score) {
    if (!isValid(score)) {
        return "分數打錯了";
    }
    if (isPass(score)) {
        return "及格";
    }
    return "不及格";
}
