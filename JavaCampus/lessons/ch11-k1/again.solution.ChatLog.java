public class ChatLog {
    // 這一學年的聊天紀錄，用程式產生
    static String[] load() {
        String[] log = new String[1000];
        for (int i = 0; i < log.length; i++) {
            if (i < 956 && i % 2 == 0) {
                log[i] = "學長今天又熬夜寫程式了";
            } else {
                log[i] = "今天的便當是滷肉飯";
            }
        }
        return log;
    }
}
