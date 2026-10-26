public class Score {
    static int total() {
        int[] points = {30, 25, 32};
        int sum = 0;
        for (int p : points) {
            sum += p;
        }
        return sum;
    }
}
