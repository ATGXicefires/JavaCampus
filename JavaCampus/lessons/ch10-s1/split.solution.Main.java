public class Main {
    public static void main(String[] args) {
        String name = "小白學程式";
        String version = "v0.9";
        System.out.println(name + " " + version);
        Question q = new Question("if-else 像什麼？", "選午餐");
        System.out.println(q.text + " " + q.check(" 選午餐 "));
    }
}
