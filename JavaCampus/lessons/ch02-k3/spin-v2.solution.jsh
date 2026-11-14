String bangs = "";
int bangCount = 0;
for (int i = 1; i <= spins; i++) {
    bangs += "！";
    bangCount += i;
    System.out.println("[" + i + "/" + spins + "] 梁亭瑄天才" + bangs);
}
System.out.println("總共轉了 " + spins + " 圈，共 " + bangCount + " 個！");
