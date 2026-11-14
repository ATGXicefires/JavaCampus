class MascotCheer implements ____ {
    String mascot;
    String tip;

    MascotCheer(String mascot, String tip) {
        this.mascot = (mascot == null || mascot.trim().isEmpty()) ? "滷肉" : mascot.trim();
        this.tip = (tip == null || tip.trim().isEmpty()) ? "保持微笑" : tip.trim();
    }

    public String sender() {
        return mascot;
    }

    public String message(int round) {
        return ____;
    }
}

class CheerBoard {
    Cheer[] cheers;

    CheerBoard(Cheer[] cheers) {
        this.cheers = cheers == null ? new Cheer[0] : cheers;
    }

    int count() {
        ____
    }

    String formatCard(int round) {
        ____
    }
}
