class Plan {
    String owner;
    int weeklyHours;

    Plan(String owner, int weeklyHours) {
        this.owner = owner;
        this.weeklyHours = weeklyHours;
    }

    @Override
    public String toString() {
        return ____;
    }
}

class MyPlan extends Plan {
    String department;
    String hobby;

    MyPlan(String owner, int weeklyHours, String department, String hobby) {
        super(owner, weeklyHours);
        this.department = department;
        this.hobby = hobby;
    }

    @Override
    public String toString() {
        return ____ + "：" + department + "，" + hobby + "（每週 " + weeklyHours + " 小時）";
    }
}

MyPlan plan = new MyPlan("梁亭瑄", 8, "資訊科", "繼續畫畫");
System.out.println(plan);
