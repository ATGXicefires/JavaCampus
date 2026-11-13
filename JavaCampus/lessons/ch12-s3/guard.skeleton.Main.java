public class Main {
    public static void main(String[] args) {
        String[] units = {"第 10 課", "第 11 課", "第 12 課", "第 13 課", "第 14 課"};
        int[] correct = {3, 4, 0, 6, -1};
        int[] count = {4, 5, 0, 5, 4};
        for (int i = 0; i < units.length; i++) {
            System.out.println(formatUnitLine(units[i], correct[i], count[i]));
        }
    }

    public static int percent(int correct, int count) {
        if (count <= 0 || correct <= 0) {
            return 0;
        }
        if (correct >= count) {
            return 100;
        }
        return (int) Math.round(correct * 100.0 / count);
    }

    public static String formatUnitLine(String unit, int correct, int count) {
        String label = (unit == null || unit.isBlank()) ? "未命名" : unit.trim();
        if (count <= 0) {
            return label + "：—";
        }
        return label + "：" + percent(correct, count) + "%";
    }

    public static int clampScore(int score) {
        ____
    }

    public static int averageScore(int[] scores) {
        ____
    }

    public static int maxScore(int[] scores) {
        ____
    }

    public static String stressReport(int[] scores) {
        ____
    }
}
