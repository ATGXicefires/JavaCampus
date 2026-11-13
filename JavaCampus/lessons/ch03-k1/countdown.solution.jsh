int studyDays = 0;
for (int i = days; i >= 1; i--) {
    studyDays++;
    if (i > 3) {
        System.out.println("段考倒數 " + i + " 天");
    } else {
        System.out.println("最後衝刺倒數 " + i + " 天");
    }
}
System.out.println("共 " + studyDays + " 天");
