package campus;

import campus.art.Art;
import campus.engine.Sandbox;
import campus.fx.Motion;
import campus.fx.Music;
import campus.fx.Sfx;
import campus.fx.Viewport;
import campus.lesson.LessonLibrary;
import campus.progress.Progress;
import campus.progress.SaveStore;
import campus.story.StoryLibrary;
import campus.ui.Game;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class App extends Application {

    /** 玩家在標題畫面按了「更新」；{@link #main} 依此決定結束代碼。 */
    private static volatile boolean updateRequested;

    private Sandbox sandbox;
    private ExecutorService runner;
    private Sfx sfx;
    private Music music;
    private Game game;

    @Override
    public void start(Stage stage) throws Exception {
        // 打包版由啟動器告訴我們資料放在哪；從原始碼執行時沒有設定，就照舊用目前的工作目錄
        Path home = Path.of(System.getProperty("campus.home", ""));
        loadFonts(home.resolve("fonts"));
        // 劇本放在專案外層的 story/，讓寫劇本的人繼續在原本的資料夾裡寫
        StoryLibrary story = StoryLibrary.load(home.resolve(Path.of("..", "story", "scripts")));
        LessonLibrary lessons = LessonLibrary.load(home.resolve("lessons"));
        SaveStore store = new SaveStore(resolveSaveFile(home, System.getProperty("campus.home") != null, System.getenv()));
        Progress progress = store.load();
        sfx = Sfx.create();
        music = Music.create(sfx);

        sandbox = new Sandbox(Duration.ofSeconds(3));
        runner = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "sandbox-runner");
            t.setDaemon(true);
            return t;
        });
        // 先開好 JShell 的遠端 JVM，玩家第一次按「執行」才不用等
        runner.submit(() -> sandbox.run(List.of(), "", List.of()));

        StackPane root = new StackPane();
        root.getStyleClass().add("app-root");
        Viewport viewport = new Viewport(root);
        game = new Game(story, lessons, new Art(home.resolve("art")), progress, store, sfx, music, sandbox, runner,
                root, stage, viewport);
        Progress.Settings settings = progress.settings();
        // 選了預設尺寸就直接用那個大小開，不用先開一個視窗再縮放
        double scale = Screen.getPrimary().getOutputScaleX();
        Scene scene = settings.windowWidth() > 0
                ? new Scene(viewport, settings.windowWidth() / scale, settings.windowHeight() / scale)
                : new Scene(viewport, 1600, 900);
        scene.getStylesheets().add(App.class.getResource("/campus/theme.css").toExternalForm());
        Motion.installPress(scene);
        game.installWindowDrag(scene);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F11) {
                e.consume();
                game.toggleFullScreen();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                e.consume();
                game.onEscape(e.getTarget());
            } else {
                game.onKey(e);
            }
        });
        scene.addEventFilter(KeyEvent.KEY_RELEASED, game::onKey);
        stage.initStyle(settings.borderless() ? StageStyle.UNDECORATED : StageStyle.DECORATED);
        stage.setScene(scene);
        stage.setTitle("JavaCampus");
        // 最小要放得下 1280×720 的預設尺寸；畫面內容由 Viewport 等比例縮放
        stage.setMinWidth(Math.min(960, 1280 / scale));
        stage.setMinHeight(Math.min(540, 720 / scale));
        stage.setMaximized(settings.windowWidth() <= 0);
        // Esc 留給設定面板，全螢幕改用 F11 切換
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        game.applySettings();
        game.showTitle();
        stage.show();
        Update.check(System.getenv("JAVACAMPUS_UPDATE_URL"), System.getProperty("jpackage.app-version"))
                .thenAcceptAsync(v -> v.ifPresent(game::offerUpdate), Platform::runLater);
    }

    @Override
    public void stop() {
        // 自動存檔只在場景開頭和選擇時寫入；關視窗時再存一次，下次才會回到正在看的那一句
        if (game != null) {
            game.save();
            updateRequested = game.updateRequested();
        }
        if (sandbox != null) {
            sandbox.stop();
        }
        if (runner != null) {
            runner.shutdownNow();
        }
        if (sandbox != null) {
            sandbox.close();
        }
        if (music != null) {
            music.close();
        }
        if (sfx != null) {
            sfx.close();
        }
    }

    /** 字型隨遊戲附帶，玩家的電腦上沒裝也能顯示；載入失敗時 CSS 會退回系統字型。 */
    private static void loadFonts(Path dir) throws IOException {
        try (Stream<Path> files = Files.walk(dir)) {
            for (Path f : files.filter(p -> p.toString().matches(".*\\.(ttf|otf)")).toList()) {
                if (Font.loadFont(f.toUri().toString(), 14) == null) {
                    System.err.println("字型載入失敗：" + f);
                }
            }
        }
    }

    /** 解析存檔路徑；打包版優先使用 %APPDATA% 並在首次啟動時平滑遷移舊存檔，亦可透過 campus.save 覆寫。 */
    static Path resolveSaveFile(Path home, boolean packaged, Map<String, String> env) {
        String custom = System.getProperty("campus.save");
        if (custom != null && !custom.isBlank()) {
            return Path.of(custom.trim());
        }

        // 單檔啟動器會傳入存檔資料夾；直接執行打包版時才自己找 %APPDATA%
        String saveDir = env.get("JAVACAMPUS_SAVE_DIR");
        String appData = env.get("APPDATA");
        Path target;
        if (saveDir != null && !saveDir.isBlank()) {
            target = Path.of(saveDir, "progress.json");
        } else if (packaged && appData != null && !appData.isBlank()) {
            target = Path.of(appData, "JavaCampus", "save", "progress.json");
        } else {
            return home.resolve(Path.of("save", "progress.json"));
        }

        // 舊版 zip 的存檔在解壓資料夾的 app\save 下；新 exe 放進（或解壓覆蓋到）那個資料夾時，啟動器會把資料夾傳進來
        String launcherDir = env.get("JAVACAMPUS_LAUNCHER_DIR");
        if (launcherDir != null && !launcherDir.isBlank()) {
            Path legacy = Path.of(launcherDir, "app", "save", "progress.json");
            if (!Files.exists(target) && Files.exists(legacy)) {
                try {
                    Path parent = target.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }
                    Files.copy(legacy, target);
                } catch (IOException e) {
                    System.err.println("舊存檔遷移失敗：" + e.getMessage());
                }
            }
        }
        return target;
    }

    public static void main(String[] args) {
        launch(args);
        if (updateRequested) {
            // 啟動器看到這個結束代碼，會先下載新版再重新開啟遊戲
            System.exit(Update.EXIT_CODE);
        }
    }
}
