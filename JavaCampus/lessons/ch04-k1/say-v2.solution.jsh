static void say(String who, String line) {
    String speaker = who.trim();
    if (speaker.isEmpty()) {
        speaker = "滷肉";
    }
    String msg = line.trim();
    if (msg.isEmpty()) {
        msg = "……";
    }
    System.out.println(speaker + "：「" + msg + "」");
}

say("滷肉", "歡迎來到小白！");
say("  ", "分號別忘記。");
