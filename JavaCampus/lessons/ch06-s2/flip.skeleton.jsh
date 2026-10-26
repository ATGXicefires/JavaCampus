class Gift {
    String to;
    String from;
    String message;
    String ps;

    Gift(String to, String from, String message) {
        this.to = to;
        this.from = from;
        this.message = message;
    }

    void open() {
        System.out.println("給 " + to);
        System.out.println(message);
        System.out.println("—— " + from);
    }

    void flip() {
        System.out.println("P.S. " + ____);
    }
}

Gift card = new Gift("梁亭瑄", "吳二弘", "感謝你這學期的 contribution。你的 code review 意見（雖然大部分是在抱怨）讓這個專案變得更好。");
card.ps = "木瓜牛奶換成冬天的話，熱可可也可以。";
card.open();
card.____();
