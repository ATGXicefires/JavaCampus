class Student {
    String name;
    int[] scores;

    Student(String name, int[] scores) {
        if (name == null || name.trim().isEmpty()) {
            this.name = "無名氏";
        } else {
            this.name = name.trim();
        }
        if (scores == null) {
            this.scores = new int[0];
        } else {
            this.scores = new int[scores.length];
            for (int i = 0; i < scores.length; i++) {
                this.scores[i] = Math.max(0, Math.min(100, ____));
            }
        }
    }

    double average() {
        if (scores.length == 0) {
            return 0.0;
        }
        int sum = 0;
        for (int i = 0; i < scores.length; i++) {
            sum += scores[i];
        }
        return (double) sum / ____;
    }

    boolean pass() {
        if (scores.length == 0 || average() < 60.0) {
            return false;
        }
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] < ____) {
                return false;
            }
        }
        return true;
    }
}
