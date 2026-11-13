static boolean isLeap(int year) {
    return year > 0 && ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0);
}

static int daysInMonth(int year, int month) {
    if (year <= 0 || month < 1 || month > 12) {
        return -1;
    }
    if (month == 2) {
        return isLeap(year) ? 29 : 28;
    }
    if (month == 4 || month == 6 || month == 9 || month == 11) {
        return 30;
    }
    return 31;
}

static int nextLeap(int year) {
    int y = year < 0 ? 1 : year + 1;
    while (!isLeap(y)) {
        y++;
    }
    return y;
}
