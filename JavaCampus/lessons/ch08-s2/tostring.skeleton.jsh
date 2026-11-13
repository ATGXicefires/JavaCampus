class Question {
    String text;
    String answer;

    Question(String text, String answer) {
        this.text = text;
        this.answer = answer;
    }

    boolean check(String typed) {
        if (typed == null || answer == null) {
            return false;
        }
        return typed.equals(answer);
    }

    @Override
    public String toString() {
        return ____;
    }
}

class FillQuestion extends Question {
    FillQuestion(String text, String answer) {
        super(text, answer);
    }

    @Override
    boolean check(String typed) {
        if (typed == null || answer == null) {
            return false;
        }
        String cleanTyped = typed.replace(" ", "");
        String cleanAnswer = answer.replace(" ", "");
        return cleanTyped.equalsIgnoreCase(cleanAnswer);
    }

    @Override
    ____ String toString() {
        int len = answer == null ? 0 : answer.replace(" ", "").length();
        return "填空題：" + text + "（答案 " + len + " 字）";
    }
}

FillQuestion q = new FillQuestion("if-else 像什麼？", "選午餐");
System.out.println(q);
