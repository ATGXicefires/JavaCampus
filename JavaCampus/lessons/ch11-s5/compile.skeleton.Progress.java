import java.util.ArrayList;

public class Progress {
    private int done;
    private ArrayList<____> scores = new ArrayList<>();

    public void finishChapter() {
        done++;
    }

    public int getDone() {
        return done;
    }

    public void recordScore(int score) {
        scores.add(score);
    }

    public int getAverage() {
        int sum = 0;
        for (int s : scores) {
            sum += s;
        }
        return sum / scores.size();
    }

    public int getPassedCount() {
        int count = 0;
        for (int s : scores) {
            if (s > 60) {
                count++;
            }
        }
        return count;
    }
}
