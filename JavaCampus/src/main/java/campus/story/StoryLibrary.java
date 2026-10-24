package campus.story;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 從 {@code story/scripts/} 讀入章節清單、每章的劇本和 {@code docs/} 裡的文件。
 *
 * @param chapters 寫好、可以玩的章節
 * @param contents 章節選擇的目錄，連還沒寫的章節也列出來
 * @param docDir   文件裡的圖片從這個資料夾找
 */
public record StoryLibrary(List<Chapter> chapters, List<Listing> contents, Map<String, Doc> docs, Path docDir) {

    /** 目錄上的一章；還沒寫的章節 {@code chapter} 是 null。 */
    public record Listing(int number, String title, Chapter chapter) {}

    private record Manifest(List<Entry> chapters) {}

    /** 沒寫 {@code dir} 的是還沒寫的章節，只出現在目錄上。 */
    private record Entry(String id, int number, String title, String dir, int progress) {}

    private static final Gson GSON = new Gson();

    /** @throws ScriptException 劇本寫錯，訊息會標出檔名和行號 */
    public static StoryLibrary load(Path root) throws IOException {
        Manifest manifest = GSON.fromJson(Files.readString(root.resolve("chapters.json"), UTF_8), Manifest.class);
        List<Chapter> chapters = new ArrayList<>();
        List<Listing> contents = new ArrayList<>();
        for (Entry e : manifest.chapters()) {
            if (e.dir() == null) {
                contents.add(new Listing(e.number(), e.title(), null));
                continue;
            }
            Path dir = root.resolve(e.dir());
            Chapter ch = new Chapter(e.id(), e.number(), e.title(), e.progress(),
                    read(dir, e.dir() + "-common.md"),
                    read(dir, e.dir() + "-" + Route.SENPAI.fileSuffix + ".md"),
                    read(dir, e.dir() + "-" + Route.KOUHAI.fileSuffix + ".md"));
            validate(ch);
            chapters.add(ch);
            contents.add(new Listing(e.number(), e.title(), ch));
        }
        Map<String, Doc> docs = new LinkedHashMap<>();
        Path docDir = root.resolve("docs");
        if (Files.isDirectory(docDir)) {
            try (Stream<Path> files = Files.list(docDir)) {
                for (Path f : files.filter(p -> p.toString().endsWith(".md")).sorted().toList()) {
                    String id = f.getFileName().toString().replaceFirst("\\.md$", "");
                    docs.put(id, Doc.parse(id, Files.readString(f, UTF_8)));
                }
            }
        }
        return new StoryLibrary(List.copyOf(chapters), List.copyOf(contents), Map.copyOf(docs), docDir);
    }

    private static ScriptFile read(Path dir, String name) throws IOException {
        return ScriptParser.parse(name, Files.readString(dir.resolve(name), UTF_8));
    }

    /** 跳轉的場景要存在：{@code @跳到}、{@code @如果}、選項只能跳同一個檔案，{@code @共通} 只能到共通檔。 */
    static void validate(Chapter ch) {
        for (ScriptFile f : List.of(ch.common(), ch.senpai(), ch.kouhai())) {
            if (f.scenes().isEmpty()) {
                throw new ScriptException(f.name(), 1, "檔案裡沒有任何內容");
            }
            for (ScriptFile.Scene scene : f.scenes()) {
                for (Op op : scene.ops()) {
                    switch (op) {
                        case Op.Jump j -> requireScene(f, j.scene(), j.line());
                        case Op.If i -> requireScene(f, i.scene(), i.line());
                        case Op.Call c -> requireScene(ch.common(), c.scene(), c.line(), f.name());
                        case Op.Menu m -> m.options().stream().filter(o -> o.target() != null)
                                .forEach(o -> requireScene(f, o.target(), m.line()));
                        default -> {
                        }
                    }
                }
            }
        }
    }

    private static void requireScene(ScriptFile f, String scene, int line) {
        requireScene(f, scene, line, f.name());
    }

    private static void requireScene(ScriptFile where, String scene, int line, String from) {
        if (where.scene(scene).isEmpty()) {
            throw new ScriptException(from, line, "找不到場景 [" + scene + "]"
                    + (where.name().equals(from) ? "" : "（要寫在 " + where.name() + "）"));
        }
    }

    public Chapter chapter(String id) {
        return chapters.stream().filter(c -> c.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("沒有這一章：" + id));
    }

    /** 下一章；已經是最後一章就是 null。 */
    public Chapter next(Chapter ch) {
        int i = chapters.indexOf(ch);
        return i >= 0 && i + 1 < chapters.size() ? chapters.get(i + 1) : null;
    }
}
