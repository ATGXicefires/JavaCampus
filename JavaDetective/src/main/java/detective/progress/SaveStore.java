package detective.progress;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class SaveStore {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Path file;

    public SaveStore(Path file) {
        this.file = file;
    }

    /** 檔案損毀時先備份成 {@code .broken} 再從頭開始，不讓一個壞檔擋住整個遊戲。 */
    public Progress load() {
        if (!Files.exists(file)) {
            return new Progress();
        }
        try {
            Progress p = GSON.fromJson(Files.readString(file, UTF_8), Progress.class);
            return p != null ? p : new Progress();
        } catch (IOException | JsonParseException e) {
            try {
                Files.move(file, file.resolveSibling(file.getFileName() + ".broken"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
                // 連備份都失敗就直接覆寫，至少遊戲能繼續
            }
            return new Progress();
        }
    }

    /** 先寫到暫存檔再搬過去，寫到一半當機也不會留下半個檔案。 */
    public void save(Progress progress) {
        try {
            Files.createDirectories(file.getParent());
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(tmp, GSON.toJson(progress), UTF_8);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException("存檔失敗：" + file, e);
        }
    }
}
