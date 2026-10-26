public class Main {
    public static void main(String[] args) {
        System.out.println("小白學程式 v0.99");
        System.out.println("總共 " + (3 + 4) + " 個貼紙");
        int[] scores = {72, 58, 65};
        int sum = 0;
        for (int i = 0; i < scores.length; i++) {
            sum += scores[i];
        }
        System.out.println("總分：" + sum);
        String typed = " 吃滷肉飯 ".trim();
        if (typed.equals("吃滷肉飯")) {
            System.out.println("答對了");
        } else {
            System.out.println("再試一次");
        }
        int score = 60;
        if (score >= 60) {
            System.out.println("及格");
        } else {
            System.out.println("不及格");
        }
        Question q = new Question("if-else 像什麼？", "選午餐");
        System.out.println(q.check(" 選午餐 "));
        Question fill = new FillQuestion("if-else 像什麼？", "選午餐");
        System.out.println(fill.check("選 午 餐"));
        Progress p = new Progress();
        p.finish();
        System.out.println("完成 " + p.getDone() + " 章");
        Drawable badge = new Badge();
        badge.draw();
    }
}
