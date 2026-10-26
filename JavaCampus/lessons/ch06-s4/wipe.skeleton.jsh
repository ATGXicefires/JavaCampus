class Glasses {
    boolean clean;

    String view() {
        if (clean) {
            return "世界很清楚";
        }
        return "世界霧霧的";
    }

    void wipe() {
        clean = ____;
    }
}

Glasses g = new Glasses();
System.out.println(g.view());
g.____();
System.out.println(g.view());
