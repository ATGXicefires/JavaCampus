static int daysToSave(int price, int perDay) {
    int saved = 0;
    int days = 0;
    while (saved < price) {
        saved += perDay;
        days++;
    }
    return ____;
}
