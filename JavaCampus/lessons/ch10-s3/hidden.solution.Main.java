public class Main {
    public static void main(String[] args) {
        Records r = new Records();
        r.record("第1課", 60);
        r.record("第2課", 80);
        r.record("第1課", 75);
        r.record("第2課", 70);
        System.out.println("第1課：" + r.bestOf("第1課"));
        System.out.println("第2課：" + r.bestOf("第2課"));
        System.out.println("第3課：" + r.bestOf("第3課"));
    }

    static int bestAfter(String[] units, int[] scores, String unit) {
        Records r = new Records();
        for (int i = 0; i < units.length; i++) {
            r.record(units[i], scores[i]);
        }
        return r.bestOf(unit);
    }
}
