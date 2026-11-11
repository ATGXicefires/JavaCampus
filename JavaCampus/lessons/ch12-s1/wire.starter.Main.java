public class Main {
    public static void main(String[] args) {
        String[] texts = {"if-else 像什麼？", "印出一行字用什麼？", "整數的型別是什麼？"};
        String[] answers = {"選午餐", "println", "int"};
        String[] typed = {" 選午餐", "print", "int"};
        Records records = new Records();
        records.record("第 1 課", 33);
        new Screen("第 1 課").show();
        // 用 ArrayList<Question> 放進 texts、answers 的 3 題，用 ExactChecker 逐題判斷 typed，印出 第 1 題：答對了 或 第 1 題：再試一次
        // 算出這次的分數（答對題數 * 100 / 題數）記進 records，再印出 records 裡這一課的最高分，例如 最高分：66
    }
}
