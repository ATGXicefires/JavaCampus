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

    public static ArrayList<String> removeBlank(ArrayList<String> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isBlank()) {
                list.remove(i);
            }
        }
        return list;
    }

    public static void main(String[] args) {
        ArrayList<Integer> scores = new ArrayList<>();
        scores.add(72);
        scores.add(58);
        scores.add(68);
        scores.add(82);
        System.out.println("及格平均：" + passAverage(scores));

        ArrayList names = new ArrayList();
        names.add("梁亭瑄");
        names.add("");
        names.add("  ");
        names.add(72);
        names.add("吳二弘");
        names.add("   ");
        names.add("李有成");

        for (int i = 0; i < names.size(); i++) {
            String s = (String) names.get(i);
            if (!s.isBlank()) {
                System.out.println(s);
            }
        }
    }
}
