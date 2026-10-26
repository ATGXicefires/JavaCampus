class Question {
    String text;
    String answer;

    Question(String text, String answer) {
        this.text = text;
        this.answer = answer;
    }

    boolean check(String typed) {
        return typed.equals(answer);
    }
}

class FillQuestion extends Question {
    FillQuestion(String text, String answer) {
        super(text, answer);
    }

    // 中間多打了空白也算對
    @Override
    boolean check(String typed) {
        return typed.replace(" ", "").equals(answer);
    }

    @Override
    public String toString() {
        return "填空題：" + answer;
    }
}

FillQuestion q = new FillQuestion("if-else 像什麼？", "選午餐");
System.out.println(q);
