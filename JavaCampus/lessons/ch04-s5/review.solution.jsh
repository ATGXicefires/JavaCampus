static String review(int sweetness) {
    if (sweetness <= 2) {
        return "可以。";
    } else if (sweetness <= 4) {
        return "甜。";
    } else {
        return "太甜。";
    }
}
