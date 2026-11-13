class Day {
    private static int total;
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

    static double getAverage() {
        if (dayCount == 0) {
            return ____;
        }
        return (double) total / dayCount;
    }

    static void resetAll() {
        total = 0;
        dayCount = 0;
        maxMentions = 0;
    }
}

for (int i = 0; i < daily.length; i++) {
    Day d = new Day(daily[i]);
    System.out.println("第 " + (i + 1) + " 天：" + d.getMentions() + " 次，累計 " + Day.getTotal() + " 次");
}
System.out.println("共 " + Day.getDayCount() + " 天，合計 " + Day.getTotal() + " 次，單日最高 " + Day.getMaxMentions() + " 次");
