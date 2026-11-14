static void say(String who, String line) {
    String speaker = who.____();
    if (speaker.isEmpty()) {
        speaker = "____";
    }
    String msg = line.____();
    if (msg.isEmpty()) {
        msg = "____";
    }
    System.out.println(speaker + "：「" + msg + "」");
}

say("滷肉", "歡迎來到小白！");
say("  ", "____");
