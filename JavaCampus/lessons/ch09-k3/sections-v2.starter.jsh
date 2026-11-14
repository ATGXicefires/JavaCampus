interface Section {
    String title();
    String format();
    int priority();
    int lineCount();
    boolean isEmpty();
}

// 請實作兩個類別：
// 1. class TextSection implements Section，建構子 TextSection(String title, String body, int priority)
// 2. class BulletSection implements Section，建構子 BulletSection(String title, String[] points, int priority)
