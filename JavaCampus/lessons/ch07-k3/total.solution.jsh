class Day {
    static int total;
    int mentions;

    Day(int mentions) {
        this.mentions = mentions;
        total += mentions;
    }
}

for (int i = 0; i < daily.length; i++) {
    new Day(daily[i]);
}
