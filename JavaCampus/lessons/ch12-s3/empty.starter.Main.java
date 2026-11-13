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
        // 請加上 count <= 0、correct <= 0、correct >= count 防護並四捨五入
        return correct * 100 / count;
    }

    public static String formatUnitLine(String unit, int correct, int count) {
        // 請處理 unit 為 null/空白、count <= 0 回傳 "名稱：—"
        return unit + "：" + percent(correct, count) + "%";
    }
}
