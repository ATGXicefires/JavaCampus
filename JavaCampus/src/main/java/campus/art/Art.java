package campus.art;

import javafx.scene.image.Image;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 圖檔插槽：{@code art/背景/<名稱>-<變體>.png}、{@code art/角色/<角色>/<表情>.png}。
 * 有圖檔就用圖檔，沒有就交給程式繪製；之後放進圖檔不需要改程式。
 */
public final class Art {

    private final Path root;
    private final Map<Path, Optional<Image>> cache = new HashMap<>();

    public Art(Path root) {
        this.root = root;
    }

    public Optional<Image> background(String name, String variant) {
        Path dir = root.resolve("背景");
        if (variant != null) {
            Optional<Image> exact = load(dir.resolve(name + "-" + variant + ".png"));
            if (exact.isPresent()) {
                return exact;
            }
        }
        return load(dir.resolve(name + ".png"));
    }

    /** 沒有這個表情的圖時退回 {@code 一般.png}，表情符號照樣用程式疊上去。 */
    public Optional<Image> figure(String person, String expression) {
        Path dir = root.resolve("角色").resolve(person);
        Optional<Image> exact = load(dir.resolve(expression + ".png"));
        return exact.isPresent() ? exact : load(dir.resolve("一般.png"));
    }

    private Optional<Image> load(Path p) {
        return cache.computeIfAbsent(p, k -> Files.isRegularFile(k)
                ? Optional.of(new Image(k.toUri().toString(), false))
                : Optional.empty());
    }
}
