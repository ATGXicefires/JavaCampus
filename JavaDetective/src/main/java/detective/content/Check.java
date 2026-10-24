package detective.content;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * 一個步驟怎麼判定過關。
 *
 * @param type     比對標準輸出，或比對 {@code exprs} 每個運算式的型別與值
 * @param exprs    {@code values} 模式下要求值的運算式，例如 {@code gap} 或 {@code isLate(2058)}
 * @param variants 換一組 setup 變數值再驗證一次，鍵是變數名、值是 Java 字面值
 */
public record Check(Mode type, List<String> exprs, List<Map<String, String>> variants) {

    public enum Mode {
        @SerializedName("output") OUTPUT,
        @SerializedName("values") VALUES
    }

    public Check {
        exprs = exprs == null ? List.of() : List.copyOf(exprs);
        variants = variants == null ? List.of() : List.copyOf(variants);
    }
}
