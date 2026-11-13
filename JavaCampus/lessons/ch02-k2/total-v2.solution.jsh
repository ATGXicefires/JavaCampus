int total = 0;
int evenTotal = 0;
for (int i = 1; i <= times; i++) {
    total += i;
    if (i % 2 == 0) {
        evenTotal += i;
    }
}
double avgSec = (double) total / times;
