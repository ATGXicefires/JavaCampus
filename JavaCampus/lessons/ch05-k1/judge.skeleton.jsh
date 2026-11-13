static boolean isValid(int score, int absent) {
    return score >= 0 && score <= 100 && absent >= 0 && absent <= 18;
}

static boolean isPass(int score, int absent) {
    return isValid(score, absent) && score >= 60 && absent <= 3;
}

static boolean canRetake(int score, int absent) {
    return isValid(score, absent) && score >= 40 && score <= 59 && absent <= 3;
}

static String judge(int score, int absent) {
    if (____isValid(score, absent)) {
        return "資料有誤";
    }
    if (isPass(score, absent)) {
        return "及格";
    }
    if (____(score, absent)) {
        return "可補考";
    }
    return "重修";
}
