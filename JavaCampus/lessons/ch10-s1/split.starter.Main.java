interface Checker {
    boolean check(String typed);
}

class Question implements Checker {
    String text;
    String answer;

    Question(String text, String answer) {
        this.text = text;
        this.answer = answer;
    }

    public boolean check(String typed) {
        return typed.trim().equals(answer);
    }
}

public class Main {
    public static void main(String[] args) {
        String name = "小白學程式";
        String version = "v0.9";
        System.out.println(name + " " + version);
        Question q = new Question("if-else 像什麼？", "選午餐");
        System.out.println(q.text + " " + q.check(" 選午餐 "));
    }
}
