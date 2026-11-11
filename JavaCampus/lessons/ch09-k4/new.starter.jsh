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

// 先 new 一個 LessonScreen、再 new 一個 QuizScreen，各 show() 一次
new Screen().show();
