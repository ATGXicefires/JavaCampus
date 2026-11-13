abstract class Screen {
    final int width;

    Screen(int width) {
        // 若 width < 12 強制夾成 12
        this.width = 0;
    }

    // 寫出 final String render() 作為樣板方法
    // 並宣告 abstract String header();、abstract String body();
    // 以及預設勾子 String subtitle()
}

// 寫出 LessonScreen extends Screen 與 QuizScreen extends Screen
