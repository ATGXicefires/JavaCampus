package campus.story;

import java.util.List;
import java.util.Optional;

/**
 * 一個劇本檔。檔案開頭在第一個 {@code [場景]} 之前就有內容時，那段會成為名稱是空字串的場景。
 */
public record ScriptFile(String name, List<Scene> scenes) {

    public record Scene(String name, List<Op> ops) {}

    public Optional<Scene> scene(String sceneName) {
        return scenes.stream().filter(s -> s.name().equals(sceneName)).findFirst();
    }

    /** 視角檔播完一個場景會接著播下一個；找不到或已經是最後一個就是 null。 */
    public Scene after(String sceneName) {
        for (int i = 0; i < scenes.size() - 1; i++) {
            if (scenes.get(i).name().equals(sceneName)) {
                return scenes.get(i + 1);
            }
        }
        return null;
    }
}
