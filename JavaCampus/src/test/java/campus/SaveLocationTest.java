package campus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SaveLocationTest {

    @TempDir
    Path tmp;

    @Test
    void runningFromSourceKeepsTheSaveInTheWorkingFolder() {
        Path home = tmp.resolve("home");
        assertEquals(home.resolve(Path.of("save", "progress.json")),
                App.resolveSaveFile(home, false, Map.of("APPDATA", tmp.toString())));
    }

    @Test
    void theLauncherDecidesWhereSavesGo() {
        Path saves = tmp.resolve("saves");
        assertEquals(saves.resolve("progress.json"),
                App.resolveSaveFile(tmp.resolve("home"), true, Map.of("JAVACAMPUS_SAVE_DIR", saves.toString())));
    }

    @Test
    void runningThePackagedGameDirectlyStillSavesUnderAppData() {
        assertEquals(tmp.resolve(Path.of("JavaCampus", "save", "progress.json")),
                App.resolveSaveFile(tmp.resolve("home"), true, Map.of("APPDATA", tmp.toString())));
    }

    @Test
    void anOldZipSaveNextToTheLauncherIsCopiedOnlyOnce() throws Exception {
        Path oldFolder = tmp.resolve("old");
        Path legacy = oldFolder.resolve(Path.of("app", "save", "progress.json"));
        Files.createDirectories(legacy.getParent());
        Files.writeString(legacy, "old");
        Map<String, String> env = Map.of(
                "JAVACAMPUS_SAVE_DIR", tmp.resolve("saves").toString(),
                "JAVACAMPUS_LAUNCHER_DIR", oldFolder.toString());

        Path save = App.resolveSaveFile(tmp.resolve("home"), true, env);
        assertEquals("old", Files.readString(save));

        // 搬過一次之後就以新位置為準，舊資料夾的檔案不會蓋掉後來的進度
        Files.writeString(save, "new");
        App.resolveSaveFile(tmp.resolve("home"), true, env);
        assertEquals("new", Files.readString(save));
    }
}
