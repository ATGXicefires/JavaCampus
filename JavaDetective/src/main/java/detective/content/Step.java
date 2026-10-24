package detective.content;

import com.google.gson.annotations.SerializedName;

/**
 * 案件裡的一個步驟。
 *
 * @param form     玩家寫的是程式片段，還是完整的 {@code Main.java}
 * @param starter  一開始放進編輯器的程式碼；為空時沿用玩家上一步過關的程式碼
 * @param skeleton 第三層提示，挖空的地方寫成 {@code ____}
 * @param solution 參考解答，同時是第四層提示
 */
public record Step(String id, String title, String instructions, String expectedShape, Form form, Check check,
                   Hints hints, String afterText, String starter, String skeleton, String solution) {

    /** 前期直接寫敘述，案件變數已經宣告好；後期寫完整的檔案，從 import、class 到 main 都自己來。 */
    public enum Form {
        @SerializedName("snippet") SNIPPET,
        @SerializedName("program") PROGRAM
    }

    public Step {
        form = form == null ? Form.SNIPPET : form;
    }

    Step withCode(String starter, String skeleton, String solution) {
        return new Step(id, title, instructions, expectedShape, form, check, hints, afterText, starter, skeleton, solution);
    }
}
