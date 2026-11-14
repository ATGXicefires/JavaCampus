____

public class Main {
    public static void main(String[] args) {
        String[] texts = {"if-else 像什麼？", "印出一行字用什麼？", "整數的型別是什麼？"};
        String[] answers = {"選午餐", "println", "int"};
        String[] typed = {" 選午餐", "print", "int"};
        Records records = new Records();
        records.record("第 1 課", 33);
        new Screen("第 1 課").show();
        ArrayList<Question> quiz = new ArrayList<>();
        ____
    }
}
