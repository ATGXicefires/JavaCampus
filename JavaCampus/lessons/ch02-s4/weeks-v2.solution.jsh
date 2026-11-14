int reviews = 0;
for (int i = 1; i <= weeks; i++) {
    if (i % 2 == 0) {
        reviews++;
        System.out.println("W" + i + " [檢討]");
    } else {
        System.out.println("W" + i + " [開發]");
    }
}
System.out.println("共 " + weeks + " 週，含 " + reviews + " 次檢討");
