// 紙條上寫的（除了語法筆誤，還藏了短路順序、邊界與運算優先序錯誤）
static boolean canPass(String name, int score, int absent, boolean extraCredit) {
    if (!name.trim().isEmpty() && name != null) {
        if (score >= 0 && score <= 100 && absent >= 0) {
            return score > 60 || extraCredit && score >= 50 && absent < 3；
        }
    }
    Return false;
}
System.out.println(canPass("亭瑄", 60, 3, false));
