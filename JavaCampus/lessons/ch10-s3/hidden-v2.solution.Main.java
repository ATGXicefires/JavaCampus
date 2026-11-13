public class Main {
    public static int bestFromLogs(String[] logs, String unit) {
        Records r = new Records(logs);
        return r.bestOf(unit);
    }

    public static double averageFromLogs(String[] logs) {
        Records r = new Records(logs);
        return r.averageBest();
    }

    public static void main(String[] args) {
        String[] logs = {
            "ch01:70",
            "ch02:80",
            "ch01:95",
            "ch02:65",
            "ch01:60"
        };
        Records r = new Records(logs);
        System.out.println("ch01：" + r.bestOf("ch01"));
        System.out.println("ch02：" + r.bestOf("ch02"));
        System.out.println("ch03：" + r.bestOf("ch03"));
        System.out.println("平均：" + r.averageBest());
    }
}
