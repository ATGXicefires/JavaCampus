interface Lunch {
    String eat();
}

class LuRouFan implements Lunch {
    public String eat() {
        return "吃滷肉飯";
    }
}

class KongRouFan implements Lunch {
    public String eat() {
        return "吃控肉飯";
    }
}

____[] week = {new LuRouFan(), new KongRouFan(), new LuRouFan(), new KongRouFan(), new LuRouFan()};
for (int i = 0; i < week.length; i++) {
    System.out.println(days[i] + "：" + week[i].____());
}
