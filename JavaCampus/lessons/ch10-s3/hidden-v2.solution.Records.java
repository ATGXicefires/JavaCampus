import java.util.HashMap;

public class Records {
    private HashMap<String, Integer> best = new HashMap<>();

    public Records() {
    }

    public Records(String[] entries) {
        recordBatch(entries);
    }

    public void record(String unit, int score) {
        if (unit == null || unit.isBlank() || score < 0 || score > 100) {
            return;
        }
        String clean = unit.trim();
        if (!best.containsKey(clean) || score > best.get(clean)) {
            best.put(clean, score);
        }
    }

    public void recordBatch(String[] entries) {
        if (entries == null) {
            return;
        }
        for (String entry : entries) {
            if (entry == null) {
                continue;
            }
            int sep = entry.indexOf(':');
            if (sep <= 0) {
                continue;
            }
            String unit = entry.substring(0, sep);
            String scoreText = entry.substring(sep + 1).trim();
            try {
                int score = Integer.parseInt(scoreText);
                record(unit, score);
            } catch (NumberFormatException e) {
                // 略過非整數分數
            }
        }
    }

    public int bestOf(String unit) {
        if (unit == null || unit.isBlank()) {
            return 0;
        }
        return best.getOrDefault(unit.trim(), 0);
    }

    public double averageBest() {
        if (best.isEmpty()) {
            return 0.0;
        }
        int sum = 0;
        for (int score : best.values()) {
            sum += score;
        }
        return (double) sum / best.size();
    }
}
