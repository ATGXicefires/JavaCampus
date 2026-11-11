import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<____> scores = new ArrayList<>();
        scores.add(72);
        scores.add(58);
        scores.add(65);
        int sum = 0;
        for (int i = 0; i < scores.size(); i++) {
            sum += scores.get(i);
        }
        System.out.println("平均：" + (double) sum / scores.size());
    }
}
