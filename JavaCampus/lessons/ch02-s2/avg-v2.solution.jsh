double targetAvg = 3.5;
int sum = today + yesterday;
double avg = (double) sum / days;
double dayShare = (double) today / sum * 100;
boolean metGoal = avg >= targetAvg;
