class Mascot {
    String name;
    String line;

    Mascot(String name, String line) {
        if (name == null || name.trim().isEmpty()) {
            this.name = "新同學";
        } else {
            this.name = name.trim();
        }
        this.line = line == null ? "" : line;
    }

    boolean hasName(String target) {
        if (target == null || this.name == null) {
            return false;
        }
        return this.name.equals(target.trim());
    }

    boolean copyLineFrom(Mascot other) {
        if (other == null || other.line == null || other.line.trim().isEmpty()) {
            return false;
        }
        this.line = other.line;
        return true;
    }
}
