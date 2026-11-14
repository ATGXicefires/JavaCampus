// 紙條上寫的（已修好語法與邏輯錯誤）
static boolean canPass(String name, int score, int absent, boolean extraCredit) {
    if (name != null && !name.trim().isEmpty()) {
        if (score >= 0 && score <= 100 && absent >= 0 && absent <= 3) {
            return (score >= 60 || (extraCredit && score >= 50));
        }
    }
    return false;
}
System.out.println(canPass("亭瑄", 60, 3, false));
