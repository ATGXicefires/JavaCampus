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
