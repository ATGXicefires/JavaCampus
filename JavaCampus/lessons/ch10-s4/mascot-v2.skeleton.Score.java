public class Score {
    public static int total() {
        int[] points = {30, 25, 32};
        return total(points);
    }

    public static int total(int[] points) {
        if (points == null || points.length == 0) {
            return 0;
        }
        int sum = 0;
        for (int p : points) {
            if (p > 0) {
                sum += p;
            }
        }
        return sum;
    }

    public static double average(int[] points) {
        if (points == null || points.length == 0) {
            return 0.0;
        }
        int sum = 0;
        int count = 0;
        for (int p : points) {
            if (p >= 0) {
                sum += p;
                count++;
            }
        }
        if (count == 0) {
            return 0.0;
        }
        return (double) sum / count;
    }
}
