import java.util.HashMap;

public class Records {
    HashMap<String, Integer> best = new HashMap<>();

    void record(String unit, int score) {
        if (!best.containsKey(unit) || score > best.get(unit)) {
            best.put(unit, score);
        }
    }

    int bestOf(String unit) {
        return best.getOrDefault(unit, 0);
    }
}
