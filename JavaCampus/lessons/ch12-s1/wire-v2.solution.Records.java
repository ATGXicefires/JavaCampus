import java.util.HashMap;

public class Records {
    HashMap<String, Integer> best = new HashMap<>();

    public void record(String unit, int score) {
        if (unit == null || unit.isBlank() || score < 0) {
            return;
        }
        if (!best.containsKey(unit) || score > best.get(unit)) {
            best.put(unit, score);
        }
    }

    public int bestOf(String unit) {
        if (unit == null) {
            return 0;
        }
        return best.getOrDefault(unit, 0);
    }
}
