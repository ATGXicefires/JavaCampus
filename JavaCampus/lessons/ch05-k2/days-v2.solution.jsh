int saved = start;
int days = 0;
if (saved < price && earn - cost <= 0) {
    days = -1;
} else {
    while (saved < price) {
        saved += earn - cost;
        days++;
    }
}
