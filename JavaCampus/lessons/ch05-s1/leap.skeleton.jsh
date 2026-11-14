static boolean isLeap(int year) {
    return year ____ 0 && ((year % 4 == 0 && year % 100 ____ 0) || year % 400 == 0);
}
