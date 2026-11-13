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
        // 請限制在 0..100
        return score;
    }

    public static int averageScore(int[] scores) {
        // 尚無 null/空陣列檢查，也未先套用 clampScore
        int sum = 0;
        for (int s : scores) {
            sum += s;
        }
        return sum / scores.length;
    }

    public static int maxScore(int[] scores) {
        // 尚無 null/空陣列檢查，也未先套用 clampScore
        int max = scores[0];
        for (int s : scores) {
            if (s > max) {
                max = s;
            }
        }
        return max;
    }

    public static String stressReport(int[] scores) {
        // 尚無 null/空陣列防護
        return "筆數=" + scores.length + ", 平均=" + averageScore(scores) + ", 最高=" + maxScore(scores);
    }
}
