int perWeek = perDay * days;
double dailyAvg = perWeek / 7.0;
boolean overQuota = perWeek > budget;
int diff = perWeek - budget;
