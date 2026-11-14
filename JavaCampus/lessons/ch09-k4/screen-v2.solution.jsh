abstract class Screen {
    final int width;

    Screen(int width) {
        this.width = Math.max(12, width);
    }

    final String render() {
        String rawHeader = header();
        String safeHeader = (rawHeader == null || rawHeader.trim().isEmpty()) ? "小白學程式" : rawHeader.trim();
        String out = "[ " + safeHeader + " ]";
        String sub = subtitle();
        if (sub != null && !sub.trim().isEmpty()) {
            out += "\n> " + sub.trim();
        }
        String rawBody = body();
        String safeBody = (rawBody == null || rawBody.trim().isEmpty()) ? "(無內容)" : rawBody.trim();
        out += "\n" + safeBody + "\n" + "-".repeat(width);
        return out;
    }

    abstract String header();

    abstract String body();

    String subtitle() {
        return "";
    }
}

class LessonScreen extends Screen {
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
        if (topic == null || topic.trim().isEmpty()) {
            return "";
        }
        return "主題：" + topic.trim();
    }
}

class QuizScreen extends Screen {
    int number;
    String question;

    QuizScreen(int width, int number, String question) {
        super(width);
        this.number = number;
        this.question = question;
    }

    String header() {
        return number <= 0 ? "隨堂測驗" : "第 " + number + " 題";
    }

    String body() {
        return question;
    }
}
