class LessonScreen {
    void show() {
        System.out.println("== 小白學程式 ==");
        System.out.println("今天的課：for 迴圈");
        System.out.println("—— 滷肉和分號陪你寫 ——");
    }
}

class QuizScreen {
    void show() {
        System.out.println("== 小白學程式 ==");
        System.out.println("第 1 題：if-else 像什麼？");
        System.out.println("—— 滷肉和分號陪你寫 ——");
    }
}

class StickerScreen {
    void show() {
        System.out.println("== 小白學程試 ==");
        System.out.println("貼紙：已收集 8 張");
        System.out.println("—— 滷肉和分號陪你寫 ——");
    }
}

new LessonScreen().show();
new QuizScreen().show();
new StickerScreen().show();
