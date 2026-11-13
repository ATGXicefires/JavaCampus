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
                this.scores[i] = Math.max(0, Math.min(100, scores[i]));
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
        return (double) sum / scores.length;
    }

    boolean pass() {
        if (scores.length == 0 || average() < 60.0) {
            return false;
        }
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] < 40) {
                return false;
            }
        }
        return true;
    }
}

class GradeBook {
    Student[] students;

    GradeBook(Student[] students) {
        if (students == null) {
            this.students = new Student[0];
        } else {
            this.students = new Student[students.length];
            for (int i = 0; i < students.length; i++) {
                this.students[i] = students[i];
            }
        }
    }

    int countPass() {
        int count = 0;
        for (int i = 0; i < students.length; i++) {
            if (students[i] != null && students[i].pass()) {
                count++;
            }
        }
        return count;
    }

    int countAbove(double line) {
        int count = 0;
        for (int i = 0; i < students.length; i++) {
            Student s = students[i];
            if (s != null && s.scores.length > ____ && s.average() >= line) {
                count++;
            }
        }
        return count;
    }

    String topStudentName() {
        Student best = null;
        for (int i = 0; i < students.length; i++) {
            Student s = students[i];
            if (s == null || s.scores.length == 0) {
                continue;
            }
            if (best == null || s.average() ____ best.average()) {
                best = s;
            }
        }
        return best == null ? "無" : best.name;
    }
}
