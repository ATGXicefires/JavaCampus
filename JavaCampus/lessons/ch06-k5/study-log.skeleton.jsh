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

class StudyLog {
    PracticeRecord[] records;
    int count;

    StudyLog(int capacity) {
        this.records = new PracticeRecord[Math.max(0, ____)];
        this.count = 0;
    }

    boolean add(PracticeRecord r) {
        if (r == null || count >= ____) {
            return false;
        }
        records[count] = r;
        count++;
        return true;
    }

    int addAll(String[] lines) {
        if (lines == null) {
            return 0;
        }
        int added = 0;
        for (int i = 0; i < lines.length; i++) {
            PracticeRecord r = PracticeRecord.parse(lines[i]);
            if (r != null && add(r)) {
                added++;
            }
        }
        return added;
    }

    int passCount() {
        int passed = 0;
        for (int i = 0; i < count; i++) {
            if (records[i].isPassed()) {
                passed++;
            }
        }
        return passed;
    }

    int totalMinutes() {
        int sum = 0;
        for (int i = 0; i < count; i++) {
            sum += records[i].minutes;
        }
        return sum;
    }

    String topTopic() {
        if (count == 0) {
            return "無";
        }
        PracticeRecord best = records[0];
        for (int i = 1; i < count; i++) {
            if (records[i].score ____ best.score) {
                best = records[i];
            }
        }
        return best.topic;
    }
}
