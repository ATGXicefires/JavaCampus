abstract class Screen {
    void show() {
        System.out.println("== 小白學程式 ==");
        // 在這裡呼叫 body()
        System.out.println("—— 滷肉和分號陪你寫 ——");
    }

    // 把 body() 寫成 abstract（沒有內容，留給子類別寫）
}

// 寫 class LessonScreen extends Screen，在 body() 印出 今天的課：for 迴圈
// 最後 new 一個 LessonScreen，呼叫它的 show()
