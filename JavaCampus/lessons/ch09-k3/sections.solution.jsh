interface Section {
    String title();

    String body();
}

class Project implements Section {
    public String title() {
        return "專題製作";
    }

    public String body() {
        return "小白學程式";
    }
}

class License implements Section {
    public String title() {
        return "乙級證照";
    }

    public String body() {
        return "電腦軟體設計";
    }
}

class Statement implements Section {
    public String title() {
        return "自傳";
    }

    public String body() {
        return "把術語翻成人話";
    }
}
