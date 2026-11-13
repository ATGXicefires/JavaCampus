abstract class Screen {
    final int width;

    Screen(int width) {
        this.width = Math.max(12, width);
    }

    final String render() {
        ____
    }

    abstract String header();

    abstract String body();

    String subtitle() {
        return "";
    }
}

class LessonScreen extends ____ {
    String topic;

    LessonScreen(int width, String topic) {
        super(width);
        this.topic = topic;
    }

    String header() {
        return "課程";
    }

    String subtitle() {
        return "章節：物件導向";
    }

    String body() {
        return ____;
    }
}

class QuizScreen extends ____ {
    int number;
    String question;

    QuizScreen(int width, int number, String question) {
        super(width);
        this.number = number;
        this.question = question;
    }

    String header() {
        return ____;
    }

    String body() {
        return question;
    }
}
