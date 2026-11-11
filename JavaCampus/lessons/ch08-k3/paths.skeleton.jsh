class Path {
    String name;

    Path(String name) {
        this.name = name;
    }

    String describe() {
        return "還不知道";
    }
}

class DesignPath extends Path {
    DesignPath() {
        super("廣設科");
    }

    @____
    String describe() {
        return name + "：畫我喜歡的東西";
    }
}

class CodePath ____ Path {
    CodePath() {
        super("資訊科");
    }

    @Override
    String describe() {
        return name + "：把我畫的東西變成會動的";
    }
}
