import java.util.ArrayList;

public class Progress {
    private int done;
    private ArrayList<Integer> scores = new ArrayList<>();

    public void finishChapter() {
        done++;
    }

    public int getDone() {
        return done;
    }

    public void recordScore(int score) {
        if (score >= 0 && score <= 100) {
            scores.add(score);
        }
    }

    public int getAverage() {
        if (scores.isEmpty()) {
            return 0;
        }
        int sum = 0;
        for (int s : scores) {
            sum += s;
        }
        return (int) Math.round((double) sum / scores.size());
    }

    public int getPassedCount() {
        int count = 0;
        for (int s : scores) {
            if (s >= 60) {
                count++;
            }
        }
        return count;
    }
}
