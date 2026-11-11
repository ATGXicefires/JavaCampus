class Day {
    static int total;
    int mentions;

    Day(int mentions) {
        this.mentions = mentions;
        total += mentions;
    }
}

for (int i = 0; i < daily.length; i++) {
    Day d = new Day(daily[i]);
    System.out.println("第 " + (i + 1) + " 天：" + d.mentions + " 次，累計 " + Day.total + " 次");
}
