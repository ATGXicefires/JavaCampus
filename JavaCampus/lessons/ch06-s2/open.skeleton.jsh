class Gift {
    String to;
    String from;
    String message;

    Gift(String to, String from, String message) {
        this.to = to;
        this.from = from;
        this.message = message;
    }

    void open() {
        System.out.println("給 " + ____);
        System.out.println(message);
        System.out.println("—— " + from);
    }
}

Gift card = new Gift("梁亭瑄", "吳二弘", "感謝你這學期的 contribution。你的 code review 意見（雖然大部分是在抱怨）讓這個專案變得更好。");
card.____();
