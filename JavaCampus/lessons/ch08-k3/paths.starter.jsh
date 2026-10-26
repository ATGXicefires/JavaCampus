class Path {
    String name;

    Path(String name) {
        this.name = name;
    }

    String describe() {
        return "還不知道";
    }
}

// DesignPath 和 CodePath 都繼承 Path
// 用 @Override 覆寫 describe()：
// 廣設科：畫我喜歡的東西
// 資訊科：把我畫的東西變成會動的
