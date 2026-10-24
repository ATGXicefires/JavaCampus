package campus.lesson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 題目裡的一個步驟。說明由簡入繁：畫面上一直看得到的只有 {@code goal}，{@code instructions} 收在「完整說明」裡，點了才展開。
 *
 * @param goal         這一步要做什麼，一句話講完，例如「把引號裡的字換掉，印出 `…`」
 * @param instructions 背景和詳細的寫法說明，可以省略
 * @param form         玩家寫的是程式片段，還是完整的 {@code .java} 檔案
 * @param starter      一開始放進編輯器的檔案；為空時沿用玩家上一步過關的程式碼
 * @param skeleton     第三層提示，挖空的地方寫成 {@code ____}
 * @param solution     參考解答，同時是第四層提示
 */
public record Step(String id, String title, String goal, String instructions, String expectedShape, Form form,
                   Check check, Hints hints, String afterText, List<SourceFile> starter, List<SourceFile> skeleton,
                   List<SourceFile> solution) {

    /** 前期直接寫敘述，題目變數已經宣告好；後期寫完整的檔案，從 import、class 到 main 都自己來。 */
    public enum Form {
        @SerializedName("snippet") SNIPPET("片段.jsh"),
        @SerializedName("program") PROGRAM("Main.java");

        /** 只有一個檔案時，分頁上顯示的檔名。 */
        public final String fileName;

        Form(String fileName) {
            this.fileName = fileName;
        }
    }

    public Step {
        form = form == null ? Form.SNIPPET : form;
        goal = goal == null ? "" : goal;
        instructions = instructions == null ? "" : instructions;
        starter = starter == null ? List.of() : List.copyOf(starter);
        skeleton = skeleton == null ? List.of() : List.copyOf(skeleton);
        solution = solution == null ? List.of() : List.copyOf(solution);
    }

    Step withCode(List<SourceFile> starter, List<SourceFile> skeleton, List<SourceFile> solution) {
        return new Step(id, title, goal, instructions, expectedShape, form, check, hints, afterText, starter, skeleton,
                solution);
    }
}
