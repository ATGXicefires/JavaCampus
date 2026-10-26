public class Main {
    public static void main(String[] args) {
        String[] log = {"二弘學長早安", "（已收回）二弘", "二弘你看這個", "（已收回）二弘二弘二弘", "學長再見"};
        System.out.println("叫了「二弘」" + countName(log, ____) + " 次（收回的不算）");
    }

    static int countName(String[] log, String name) {
        int n = 0;
        for (String line : log) {
            if (line.startsWith("（已收回）")) {
                continue;
            }
            if (line.contains(name)) {
                n++;
            }
        }
        return n;
    }
}
