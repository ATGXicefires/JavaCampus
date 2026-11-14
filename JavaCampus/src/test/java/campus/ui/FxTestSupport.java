package campus.ui;

import javafx.application.Platform;
import javafx.scene.text.Font;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** 測試共用的 JavaFX 單例初始化工具，避免多個測試重複啟動 Toolkit 或反覆載入字型。 */
public final class FxTestSupport {

    /** JavaFX 的樣式和字型快取不能同時被好幾條執行緒碰，用到 JavaFX 的測試類別都掛這把鎖，輪流執行。 */
    public static final String LOCK = "javafx";

    private static boolean started;
    private static boolean fontsLoaded;

    private FxTestSupport() {
    }

    public static synchronized void ensureToolkit() {
        if (started) {
            return;
        }
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException alreadyRunning) {
            // 另一個路徑已先啟動 JavaFX Toolkit
        }
        started = true;
    }

    public static synchronized void loadFonts() {
        ensureToolkit();
        if (fontsLoaded) {
            return;
        }
        for (String f : List.of("SourceHanSans/SourceHanSansTC-Regular.otf",
                "SourceHanSans/SourceHanSansTC-Bold.otf",
                "MapleMono/MapleMono-NF-CN-Regular.ttf")) {
            assertNotNull(Font.loadFont(Path.of("fonts", f).toUri().toString(), 12), f);
        }
        fontsLoaded = true;
    }
}
