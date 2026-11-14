double avg = 0.0;
if (subjects > 0) {
    avg = (double) total / subjects;
}
if (avg >= 80.0) {
    System.out.println("穩定通過");
} else if (avg >= 60.0) {
    System.out.println("低空及格");
} else {
    System.out.println("再加油");
}
