int stopTotal = stopHour * 60 + stopMinute;
int roundTotal = roundHour * 60 + roundMinute;
int gap = stopTotal - roundTotal;
System.out.println("巡邏後 " + gap + " 分鐘斷電");
