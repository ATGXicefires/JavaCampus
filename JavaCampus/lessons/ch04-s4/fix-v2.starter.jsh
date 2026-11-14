// 修好 hint(String answer)：防護 null、空字串、單字元與前後全半形空白
// 保留第 1 個字，其餘非空白字元換成 '_'，空白保留為半形 ' '
static String hint(String answer) {
    return answer.substring(1, 2) + "＿".repeat(answer.length());
}
