// 紙條上寫的（請修好語法與邏輯錯誤）
static boolean canPass(String name, int score, int absent, boolean extraCredit) {
    if (____ != null && !name.trim().isEmpty()) {
        if (score >= 0 && score <= 100 && absent >= 0 && absent ____ 3) {
            return (score ____ 60 || (extraCredit && score >= 50));
        }
    }
    return false;
}
System.out.println(canPass("亭瑄", 60, 3, false));
