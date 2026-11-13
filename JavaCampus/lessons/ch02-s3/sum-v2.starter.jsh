// 修好覆寫錯誤，算出 total（偶數遍 +2 秒）、oddCount（奇數遍次數）、skipSum（奇數遍基本秒數總和）
int total = 0;
int oddCount = 0;
int skipSum = 0;
for (int i = 1; i <= times; i++) {
    total = i;
    if (i % 2 == 1) {
        oddCount = 1;
    }
}
