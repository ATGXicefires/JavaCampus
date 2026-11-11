// 寫 interface Checker，裡面只有 boolean check(String typed);
// 再寫兩個類別實作它，typed 是使用者打的答案：
// 填空題的 ExactChecker(String answer)：typed 去掉前後空白後和 answer 完全一樣才對
// 選擇題的 OptionChecker(String letter)：typed 和選項代號 letter 一樣就對
