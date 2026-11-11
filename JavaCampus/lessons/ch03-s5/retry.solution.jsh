int retry = 0;
for (int i = 0; i < scores.length; i++) {
    if (scores[i] < 60) {
        retry++;
    }
}
