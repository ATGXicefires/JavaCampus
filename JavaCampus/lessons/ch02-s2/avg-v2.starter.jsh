// 修好 avg 與 dayShare 的整數除法截斷，並補上 boolean metGoal
double targetAvg = 3.5;
int sum = today + yesterday;
double avg = (double) (sum / days);
double dayShare = today / sum * 100;
