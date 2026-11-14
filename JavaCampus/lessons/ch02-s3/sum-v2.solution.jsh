int total = 0;
int oddCount = 0;
int skipSum = 0;
for (int i = 1; i <= times; i++) {
    total += i;
    if (i % 2 == 0) {
        total += 2;
    } else {
        oddCount++;
        skipSum += i;
    }
}
