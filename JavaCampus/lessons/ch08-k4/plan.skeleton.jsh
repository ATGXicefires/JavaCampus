class MyPlan {
    String name = "梁亭瑄";
    String department = "資訊科";
    String hobby = "繼續畫畫";

    @Override
    public String ____() {
        return name + "的計畫：" + department + "，" + hobby;
    }
}

MyPlan plan = new MyPlan();
System.out.println(plan);
