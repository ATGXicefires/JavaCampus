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
    }
}
