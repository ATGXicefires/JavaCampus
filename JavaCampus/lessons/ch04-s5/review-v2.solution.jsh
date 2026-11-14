static String review(int sweetness, int star) {
    if (sweetness < 1 || sweetness > 5 || star < 1 || star > 5) {
        return "資料異常";
    }
    if (star >= 4 && sweetness <= 3) {
        return "招牌必點";
    } else if (sweetness >= 5) {
        return "太甜小心";
    } else if (star <= 2) {
        return "尚待改進";
    } else {
        return "表現普通";
    }
}
