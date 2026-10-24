package campus.lesson;

import java.util.List;
import java.util.Optional;

/** 遊戲內的「程式基礎教學筆記」：二弘寫給亭瑄的那份 PDF，條目隨程式題解鎖。 */
public record Handbook(List<Entry> entries) {

    /**
     * @param summary 一句話的重點，可以用 {@code `程式碼`}；筆記欄平常只顯示這句，想看完整內容再展開
     * @param body    簡單的 Markdown
     */
    public record Entry(String id, int chapter, String title, String summary, String body) {}

    public Handbook {
        entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public Optional<Entry> entry(String id) {
        return entries.stream().filter(e -> e.id().equals(id)).findFirst();
    }
}
