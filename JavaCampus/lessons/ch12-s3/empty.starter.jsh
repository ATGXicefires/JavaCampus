public class Main {
    public static void main(String[] args) {
        String[] units = {"第 10 課", "第 11 課", "第 12 課"};
        int[] correct = {3, 4, 0};
        int[] count = {4, 5, 0};
        for (int i = 0; i < units.length; i++) {
            System.out.println(units[i] + "：" + percent(correct[i], count[i]) + "%");
        }
    }

    static int percent(int correct, int count) {
        return correct * 100 / count;
    }
}
