public class Mascot {
<<<<<<< 吳二弘
    public static String greet() {
        return greet("同學");
    }

    public static String greet(String user) {
        String name = "滷肉飯";
        String target = (user == null || user.isBlank()) ? "同學" : user.trim();
        return name + "：「歡迎回來，" + target + "！」";
    }

    public static String encourage(int score) {
        String name = "滷肉飯";
        if (score >= 60) {
            return name + "：「表現不錯，繼續加油！」";
        }
        return name + "：「沒關係，再檢查一次邊界！」";
    }
=======
    public static String greet() {
        String name = "滷肉";
        return name + "：「歡迎回來！」";
    }

    public static String greet(String user) {
        String name = "滷肉";
        return name + "：「歡迎回來，" + user.trim() + "！」";
    }
>>>>>>> 梁亭瑄
}
