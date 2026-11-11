abstract class Screen {
    void show() {
        System.out.println("== 小白學程式 ==");
        body();
        System.out.println("—— 滷肉和分號陪你寫 ——");
    }

    ____ void body();
}

class LessonScreen ____ Screen {
    void body() {
        System.out.println("今天的課：for 迴圈");
    }
}

new LessonScreen().show();
