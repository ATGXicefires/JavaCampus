class PracticeRecord {
    String topic;
    int score;
    int minutes;

    PracticeRecord(String topic, int score, int minutes) {
        if (topic == null || topic.trim().isEmpty()) {
            this.topic = "未分類";
        } else {
            this.topic = topic.trim();
        }
        this.score = Math.max(0, Math.min(100, score));
        this.minutes = Math.max(0, minutes);
    }

    boolean isPassed() {
        return score >= 60;
    }

    static PracticeRecord parse(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split(",", -1);
        if (parts.length != 3) {
            return null;
        }
        try {
            int s = Integer.parseInt(parts[1].trim());
            int m = Integer.parseInt(parts[2].trim());
            return new PracticeRecord(parts[0], s, m);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
