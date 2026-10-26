public class Main {
    public static void main(String[] args) {
        String[] units = {"第 10 課", "第 11 課", "第 12 課"};
        int[] correct = {3, 4, 0};
        int[] count = {4, 5, 0};
        for (int i = 0; i < units.length; i++) {
            if (count[i] == 0) {
                System.out.println(units[i] + "：—");
            } else {
                System.out.println(units[i] + "：" + percent(correct[i], count[i]) + "%");
            }
        }
    }

    static int percent(int correct, int count) {
        if (count == 0) {
            return 0;
        }
        return correct * 100 / count;
    }
}
