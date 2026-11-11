static String review(int sweetness) {
    if (sweetness <= 2) {
        return "可以。";
    } else if (sweetness <= 4) {
        return "甜。";
    } else {
        return "太甜。";
    }
}

for (int i = 0; i < flavors.length; i++) {
    System.out.println(flavors[i] + "：" + review(____));
}
