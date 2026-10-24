package detective.content;

/**
 * 案件裡的一個步驟。
 *
 * @param starter  一開始放進編輯器的程式碼；為空時沿用玩家上一步過關的程式碼
 * @param skeleton 第三層提示，挖空的地方寫成 {@code ____}
 * @param solution 參考解答，同時是第四層提示
 */
public record Step(String id, String title, String instructions, String expectedShape, Check check, Hints hints,
                   String afterText, String starter, String skeleton, String solution) {

    Step withCode(String starter, String skeleton, String solution) {
        return new Step(id, title, instructions, expectedShape, check, hints, afterText, starter, skeleton, solution);
    }
}
