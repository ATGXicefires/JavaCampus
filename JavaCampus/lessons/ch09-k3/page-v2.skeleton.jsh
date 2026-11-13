class PortfolioPage {
    Section[] included;
    int linesUsed;

    PortfolioPage(Section[] rawSections, int maxLines) {
        int limit = Math.max(0, maxLines);
        ____
    }

    int includedCount() {
        return included.length;
    }

    int usedLines() {
        return linesUsed;
    }

    String render() {
        ____
    }
}
