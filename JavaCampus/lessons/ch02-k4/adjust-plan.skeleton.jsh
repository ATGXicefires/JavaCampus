int perWeek = perDay * days;
double dailyAvg = perWeek / 7.0;
boolean overQuota = perWeek > budget;
int diff = perWeek - budget;
int nextQuota = budget;
if (____) {
    nextQuota = ____;
    System.out.println("超標 " + diff + " 個，需要減量");
} else if (____) {
    System.out.println("剛好達標：0 個餘額");
} ____ {
    int remain = budget - perWeek;
    System.out.println("餘額 " + remain + " 個，週末每天 " + (remain ____ 2) + " 個，剩 " + (remain ____ 2) + " 個");
}
