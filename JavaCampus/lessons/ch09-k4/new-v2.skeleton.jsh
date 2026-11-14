class SummaryScreen extends ____ {
    int passed;
    int total;

    SummaryScreen(int width, int passed, int total) {
        super(width);
        ____
    }

    String header() {
        return "學習結算";
    }

    String subtitle() {
        return ____;
    }

    String body() {
        return "完成 " + passed + " / " + total + " 題";
    }
}

class ScreenRouter {
    Screen[] screens;

    ScreenRouter(Screen[] screens) {
        this.screens = screens == null ? new Screen[0] : screens;
    }

    int count() {
        ____
    }

    String renderAt(int index) {
        ____
    }

    String renderAll() {
        ____
    }
}
