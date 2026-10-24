package campus.lesson;

import java.util.List;
import java.util.Map;
import java.util.Random;

/** 寫程式時旁邊的人說的話，依搭檔和執行結果分組，每次隨機挑一句。 */
public record Reactions(Map<String, Map<String, List<String>>> byPartner) {

    public enum Kind {
        COMPILE("compile"), RUNTIME("runtime"), WRONG("wrong"), STEP("step"), DONE("done");

        public final String key;

        Kind(String key) {
            this.key = key;
        }
    }

    public boolean has(String partner) {
        return byPartner.containsKey(partner);
    }

    /** 沒有對應的台詞就是 null，畫面就不出現氣泡。 */
    public String pick(String partner, Kind kind, Random rnd) {
        List<String> lines = byPartner.getOrDefault(partner, Map.of()).getOrDefault(kind.key, List.of());
        return lines.isEmpty() ? null : lines.get(rnd.nextInt(lines.size()));
    }
}
