class Day {
    private ____ int total;
    private static int dayCount;
    private static int maxMentions;
    private int mentions;

    Day(int mentions) {
        if (mentions < 0) {
            mentions = 0;
        }
        this.mentions = mentions;
        total += mentions;
        dayCount++;
        if (mentions > maxMentions) {
            maxMentions = mentions;
        }
    }

    int getMentions() {
        return mentions;
    }

    static int getTotal() {
        return total;
    }

    static int getDayCount() {
        return dayCount;
    }

    static int getMaxMentions() {
        return maxMentions;
    }

    static void resetAll() {
        total = 0;
        dayCount = ____;
        maxMentions = 0;
    }
}

for (int i = 0; i < daily.length; i++) {
    new Day(daily[i]);
}
