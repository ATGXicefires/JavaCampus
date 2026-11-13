static int daysToSave(int price, int start, int earn, int cost) {
    if (price < 0 || start < 0 || earn < 0 || cost < 0) {
        return -1;
    }
    if (start >= price) {
        return 0;
    }
    if (earn - cost <= 0) {
        return -1;
    }
    int saved = start;
    int days = 0;
    while (saved < price) {
        saved += earn - cost;
        days++;
    }
    return days;
}
