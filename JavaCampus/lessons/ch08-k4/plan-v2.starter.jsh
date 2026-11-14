class Plan {
    String owner;
    int weeklyHours;

    Plan(String owner, int weeklyHours) {
        this.owner = owner;
        this.weeklyHours = weeklyHours;
    }

    // 覆寫 public String toString()，回傳 owner + "的計畫"
}

class MyPlan extends Plan {
    String department;
    String hobby;

    MyPlan(String owner, int weeklyHours, String department, String hobby) {
        super(owner, weeklyHours);
        this.department = department;
        this.hobby = hobby;
    }

    // 覆寫 public String toString()，使用 super.toString() 延伸格式
}

MyPlan plan = new MyPlan("梁亭瑄", 8, "資訊科", "繼續畫畫");
System.out.println(plan);
