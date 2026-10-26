static String hint(String answer) {
    return answer.substring(0, 1) + "＿".repeat(answer.length() - 1);
}

static boolean isCorrect(String typed, String answer) {
    return typed.____().equals(answer);
}
