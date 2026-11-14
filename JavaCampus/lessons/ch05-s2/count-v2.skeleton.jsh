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
    while (d <= n / d) {
        if (n % d == 0) {
            return false;
        }
        d += 2;
    }
    return true;
}

static int countPrimes(int a, int b) {
    int count = 0;
    for (int n = a; n <= b; n++) {
        if (____(n)) {
            count++;
        }
    }
    return count;
}

static int kthPrime(int k) {
    if (k <= 0) {
        return -1;
    }
    int found = 0;
    int cand = 1;
    while (found ____ k) {
        cand++;
        if (isPrime(cand)) {
            found++;
        }
    }
    return cand;
}
