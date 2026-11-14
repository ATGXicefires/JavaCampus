// 修好這段程式：印出兩天的總數 sum，和離目標還差幾個 remain
int targetAvg = 3.5;
int sum = today + yesterday;
System.out.println("共 " + today + yesterday + " 個，距離目標還差 " + (targetAvg * days - sum) + " 個");
