package campus.story;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** 劇情裡可以打開來看的文件，每頁是一段簡單的 Markdown。 */
public record Doc(String id, Skin skin, List<String> pages) {

    public enum Skin {
        PDF("PDF"), JARGON("術語文件"), CODE("程式檔"), HANDWRITTEN("手寫");

        public final String label;

        Skin(String label) {
            this.label = label;
        }

        public static Optional<Skin> byLabel(String label) {
            return Arrays.stream(values()).filter(s -> s.label.equals(label)).findFirst();
        }
    }

    /** 第一行是 {@code 外觀：PDF}，其餘是內容，單獨一行的 {@code ---} 換頁。 */
    public static Doc parse(String id, String text) {
        String[] lines = text.replace("\r\n", "\n").split("\n", 2);
        String head = lines[0].strip();
        String label = head.replaceFirst("^外觀[：:]\\s*", "");
        Skin skin = Skin.byLabel(label).orElseThrow(() -> new IllegalStateException(
                "docs/" + id + ".md 第 1 行要寫外觀：PDF、術語文件、程式檔、手寫 其中一個"));
        String body = lines.length > 1 ? lines[1] : "";
        List<String> pages = Arrays.stream(body.split("(?m)^---\\s*$")).map(String::strip)
                .filter(p -> !p.isEmpty()).toList();
        return new Doc(id, skin, pages);
    }
}
