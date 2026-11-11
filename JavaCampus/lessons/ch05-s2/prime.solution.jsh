static boolean isPrime(int n) {
    if (n < 2) {
        return false;
    }
    int d = 2;
    while (d < n) {
        if (n % d == 0) {
            return false;
        }
        d++;
    }
    return true;
}
