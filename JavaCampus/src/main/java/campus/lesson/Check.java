package campus.lesson;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * 一個步驟怎麼判定過關。
 *
 * @param type     {@code output} 比對標準輸出，{@code values} 不看輸出；兩種都會接著比 {@code exprs}
 * @param exprs    跑完之後要求值的運算式，例如 {@code gap} 或 {@code isLate(2058)}，比型別與值；回傳 {@code void}
 *                 的呼叫比它印出來的字。{@code values} 模式一定要有
 * @param variants 換一組 setup 變數值再驗證一次，鍵是變數名、值是 Java 字面值
 * @param uses     這一步一定要寫出來的寫法，例如 {@code private int count}；結果都對了才看，一個詞一個詞比，略過註解
 */
public record Check(Mode type, List<String> exprs, List<Map<String, String>> variants, List<String> uses) {

    public enum Mode {
        @SerializedName("output") OUTPUT,
        @SerializedName("values") VALUES
    }

    public Check {
        exprs = exprs == null ? List.of() : List.copyOf(exprs);
        variants = variants == null ? List.of() : List.copyOf(variants);
        uses = uses == null ? List.of() : List.copyOf(uses);
    }

    public Check(Mode type, List<String> exprs, List<Map<String, String>> variants) {
        this(type, exprs, variants, null);
    }
}
