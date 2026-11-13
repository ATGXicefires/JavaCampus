import java.util.ArrayList;

public class Main {
    public static double passAverage(ArrayList<Integer> scores) {
        if (scores == null) {
            return 0.0;
        }
        int sum = 0;
        int count = 0;
        for (Integer s : scores) {
            if (s != null && s >= 60) {
                sum += s;
                count++;
            }
        }
        if (count == 0) {
            return 0.0;
        }
        return (double) sum / count;
    }

    public static void main(String[] args) {
        ArrayList<Integer> scores = new ArrayList<>();
        scores.add(72);
        scores.add(58);
        scores.add(68);
        scores.add(82);
        System.out.println("及格平均：" + passAverage(scores));
    }
}
