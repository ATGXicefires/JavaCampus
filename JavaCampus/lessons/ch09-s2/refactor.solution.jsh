abstract class Screen {
    void show() {
        System.out.println("== 小白學程式 ==");
        body();
        System.out.println("—— 滷肉和分號陪你寫 ——");
    }

    abstract void body();
}

class LessonScreen extends Screen {
    void body() {
        System.out.println("今天的課：for 迴圈");
    }
}

class QuizScreen extends Screen {
    void body() {
        System.out.println("第 1 題：if-else 像什麼？");
    }
}

class StickerScreen extends Screen {
    void body() {
        System.out.println("貼紙：已收集 8 張");
    }
}

new LessonScreen().show();
new QuizScreen().show();
new StickerScreen().show();
