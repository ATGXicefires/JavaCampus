// 只露出答案的第一個字，其他每個字換成一個 ＿
static String hint(String answer) {
    return answer.substring(1, 2) + "＿".repeat(answer.length());
}
