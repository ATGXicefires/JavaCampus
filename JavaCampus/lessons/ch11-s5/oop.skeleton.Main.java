import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        System.out.println("小白學程式 v1.0");
        System.out.println(new Badge().renderSummary(3, 4));
        ArrayList<Integer> initScores = new ArrayList<>();
        initScores.add(72);
        initScores.add(58);
        initScores.add(65);
        System.out.println("總分：" + sumScores(initScores));
        Question[] qs = {
            new Question("if-else 像什麼？", "選午餐"),
            new FillQuestion("if-else 像什麼？", "選午餐")
        };
        String[] ans = {" 選午餐 ", "選 午 餐"};
        int[] sessionScores = {60, 80, -10, 120};
        System.out.println(evaluateSession(qs, ans, sessionScores));
    }

    public static int sumScores(ArrayList<Integer> list) {
        int sum = 0;
        for (int i = 0; i < list.size(); i++) {
            sum += list.get(i);
        }
        return sum;
    }

    public static int maxScore(ArrayList<Integer> list) {
        int max = 0;
        for (int s : list) {
            if (s > max) {
                max = s;
            }
        }
        return max;
    }

    public static boolean checkPolymorphic(Question q, String typed) {
        return q != null && q.check(typed);
    }

    public static int probeProgress(int s1, int s2) {
        Progress p = new Progress();
        p.finishChapter();
        p.recordScore(s1);
        p.recordScore(s2);
        return p.getDone() * 1000 + p.getAverage();
    }

    public static int probePassedCount(int[] scores) {
        Progress p = new Progress();
        if (scores != null) {
            for (int s : scores) {
                p.recordScore(s);
            }
        }
        return p.getPassedCount();
    }

    public static String evaluateSession(Question[] questions, String[] typed, int[] scores) {
        Progress p = new Progress();
        int correct = 0;
        int len = Math.min(questions.length, typed.length);
        for (int i = 0; i < len; i++) {
            if (questions[i].check(typed[i])) {
                correct++;
                p.finishChapter();
            }
        }
        for (int s : scores) {
            p.recordScore(s);
        }
        Drawable badge = new Badge();
        return "答對 " + correct + " 題 | 平均 " + p.getAverage() + " | " + badge.render(p.getAverage(), p.getDone());
    }
}
