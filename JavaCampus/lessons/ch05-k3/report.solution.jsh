static int toScore(String s) {
    if (s == null) {
        return -1;
    }
    try {
        int score = Integer.parseInt(s.trim());
        if (score < 0 || score > 100) {
            return -1;
        }
        return score;
    } catch (NumberFormatException e) {
        return -1;
    }
}

static double validAverage(String[] raw) {
    if (raw == null) {
        return 0.0;
    }
    int sum = 0;
    int count = 0;
    for (int i = 0; i < raw.length; i++) {
        int score = toScore(raw[i]);
        if (score >= 0) {
            sum += score;
            count++;
        }
    }
    if (count == 0) {
        return 0.0;
    }
    return (double) sum / count;
}

static int passCount(String[] raw) {
    if (raw == null) {
        return 0;
    }
    int count = 0;
    for (int i = 0; i < raw.length; i++) {
        if (toScore(raw[i]) >= 60) {
            count++;
        }
    }
    return count;
}

static int maxValid(String[] raw) {
    if (raw == null) {
        return -1;
    }
    int best = -1;
    for (int i = 0; i < raw.length; i++) {
        int score = toScore(raw[i]);
        if (score >= 0 && score > best) {
            best = score;
        }
    }
    return best;
}
