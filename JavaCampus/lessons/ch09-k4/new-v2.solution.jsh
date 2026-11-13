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

class SummaryScreen extends Screen {
    int passed;
    int total;

    SummaryScreen(int width, int passed, int total) {
        super(width);
        if (total < 0) {
            this.total = 0;
            this.passed = 0;
        } else {
            this.total = total;
            if (passed < 0) {
                this.passed = 0;
            } else if (passed > total) {
                this.passed = total;
            } else {
                this.passed = passed;
            }
        }
    }

    String header() {
        return "學習結算";
    }

    String subtitle() {
        return (total > 0 && passed == total) ? "全部通過" : "繼續練習";
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
        int c = 0;
        for (int i = 0; i < screens.length; i++) {
            if (screens[i] != null) {
                c++;
            }
        }
        return c;
    }

    String renderAt(int index) {
        if (index < 0 || index >= screens.length || screens[index] == null) {
            return "(無效畫面)";
        }
        return screens[index].render();
    }

    String renderAll() {
        String out = "";
        for (int i = 0; i < screens.length; i++) {
            if (screens[i] != null) {
                if (!out.isEmpty()) {
                    out += "\n\n";
                }
                out += screens[i].render();
            }
        }
        return out;
    }
}
