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

// 照著寫 License 和 Statement，都實作 Section：
// License 的 title() 交回 乙級證照，body() 交回 電腦軟體設計
// Statement 的 title() 交回 自傳，body() 交回 把術語翻成人話
