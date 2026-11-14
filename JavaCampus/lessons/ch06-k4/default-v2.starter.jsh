class Mascot {
    String name;
    String line;

    Mascot(String name, String line) {
        this.name = name;
        this.line = line;
    }

    boolean hasName(String target) {
        return name.equals(target);
    }

    boolean copyLineFrom(Mascot other) {
        this.line = other.line;
        return true;
    }
}
