int fullBags = gacha / packSize;
int left = gacha % packSize;
int totalBags = (gacha + packSize - 1) / packSize;
System.out.println("裝滿 " + fullBags + " 袋，剩 " + left + " 個，共需 " + totalBags + " 袋");
