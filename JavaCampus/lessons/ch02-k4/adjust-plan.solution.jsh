int perWeek = perDay * days;
double dailyAvg = perWeek / 7.0;
boolean overQuota = perWeek > budget;
int diff = perWeek - budget;
int nextQuota = budget;
if (overQuota) {
    nextQuota = budget - 1;
    System.out.println("超標 " + diff + " 個，需要減量");
} else if (perWeek == budget) {
    System.out.println("剛好達標：0 個餘額");
} else {
    int remain = budget - perWeek;
    System.out.println("餘額 " + remain + " 個，週末每天 " + (remain / 2) + " 個，剩 " + (remain % 2) + " 個");
}
