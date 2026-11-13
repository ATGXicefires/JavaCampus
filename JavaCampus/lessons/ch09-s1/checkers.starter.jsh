interface Checker {
    boolean check(String typed);
}

// 寫兩個類別實作 Checker，typed 是使用者打的答案：
// 填空題的 ExactChecker(String answer)：typed 去掉前後空白後和 answer 完全一樣才對
// 選擇題的 OptionChecker(String letter)：typed 和選項代號 letter 一樣就對
