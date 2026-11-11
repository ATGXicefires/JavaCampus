import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<Integer> scores = new ArrayList<>();
        scores.add(72);
        scores.add(58);
        scores.add(65);
        int sum = 0;
        for (int i = 0; i < scores.size(); i++) {
            sum += scores.get(i);
        }
        System.out.println("平均：" + (double) sum / scores.size());
        ArrayList<String> names = new ArrayList<>();
        names.add("梁亭瑄");
        names.add("吳二弘");
        names.add("李有成");
        for (int i = 0; i < names.size(); i++) {
            System.out.println(names.get(i));
        }
    }

    static int total(ArrayList<Integer> list) {
        int sum = 0;
        for (int n ____ list) {
            sum += n;
        }
        return sum;
    }
}
