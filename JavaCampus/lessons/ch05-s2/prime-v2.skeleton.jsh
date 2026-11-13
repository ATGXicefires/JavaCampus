static boolean isPrime(int n) {
    if (n <= 1) {
        return false;
    }
    if (n == 2) {
        return true;
    }
    if (n % 2 == 0) {
        return false;
    }
    int d = 3;
    while (d <= ____) {
        if (n % d == 0) {
            return false;
        }
        d += ____;
    }
    return true;
}
