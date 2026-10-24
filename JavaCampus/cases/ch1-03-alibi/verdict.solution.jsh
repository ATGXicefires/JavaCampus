int leaveTotal = homeHour * 60 + homeMinute - driveMinutes;
boolean stillInside = leaveTotal > stopTotal;
if (stillInside) {
    System.out.println("說謊");
} else {
    System.out.println("可信");
}
