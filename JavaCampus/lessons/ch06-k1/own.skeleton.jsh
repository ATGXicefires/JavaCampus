class Item {
    String name;
    String kind;
}

Item laptop = new Item();
laptop.name = "小白";
laptop.kind = "筆電";
System.out.println(laptop.name + "（" + laptop.kind + "）");
Item usb = new Item();
usb.name = "阿隨";
usb.kind = "USB";
System.out.println(usb.name + "（" + usb.kind + "）");
____.name = "小白二號";
System.out.println(laptop.name + "（" + laptop.kind + "）");
System.out.println(usb.name + "（" + usb.kind + "）");
