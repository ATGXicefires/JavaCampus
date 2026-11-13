import java.util.ArrayList;

public class Main {
    public static double passAverage(ArrayList<Integer> scores) {
        int sum = 0;
        int count = 0;
        for (int s : scores) {
            if (s >= 60) {
                sum += s;
                count++;
            }
        }
        return sum / count;
    }

    public static void main(String[] args) {
        ArrayList<int> scores = new ArrayList<>();
        scores.add(72);
        scores.add(58);
        scores.add(68);
        scores.add(82);
        System.out.println("及格平均：" + passAverage(scores));
    }
}
