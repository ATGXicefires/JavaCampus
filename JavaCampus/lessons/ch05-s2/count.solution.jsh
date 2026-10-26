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

static int countPrimes(int limit) {
    int count = 0;
    for (int n = 2; n <= limit; n++) {
        if (isPrime(n)) {
            count++;
        }
    }
    return count;
}
