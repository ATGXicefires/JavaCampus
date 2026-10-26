package campus.ui;

import campus.engine.Sandbox;
import campus.fx.Sfx;
import campus.fx.Viewport;
import campus.lesson.LessonLibrary;
import campus.progress.Progress;
import campus.progress.SaveStore;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LessonViewTest {

    private static Sandbox sandbox;
    private static Sfx sfx;

    @BeforeAll
    static void start() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException alreadyRunning) {
            // 別的測試已經把 JavaFX 開起來了
        }
        sandbox = new Sandbox(Duration.ofSeconds(3));
        sfx = Sfx.create();
        sfx.setVolumes(0, 0, 0, 0);
    }

    @AfterAll
    static void stop() {
        sandbox.close();
        sfx.close();
    }

    @Test
    void theBackArrowWaitsForARunningCheck(@TempDir Path dir) throws Exception {
        ExecutorService runner = Executors.newSingleThreadExecutor();
        CountDownLatch gate = new CountDownLatch(1);
        // 先塞一個卡住的工作：按「完成」之後的驗證排在它後面，就會停在還沒跑完
        runner.submit(() -> {
            gate.await();
            return null;
        });
        AtomicBoolean left = new AtomicBoolean();
        LessonView view = onFx(() -> {
            StackPane root = new StackPane();
            // 題目畫面用不到劇本、圖和音樂
            Game game = new Game(null, LessonLibrary.load(Path.of("lessons")), null, new Progress(),
                    new SaveStore(dir.resolve("progress.json")), sfx, null, sandbox, runner, root, new Stage(),
                    new Viewport(root));
            LessonView v = new LessonView(game, game.lessons.lesson("ch01-k1"), game.progress.practiceLessons(), true,
                    () -> left.set(true));
            // 「完成」在 SplitPane 裡，要套過樣式、長出外觀之後才在節點樹上找得到
            new Scene(v).getRoot().applyCss();
            return v;
        });
        Button back = button(view, "←");
        Button check = button(view, "完成");

        runFx(check::fire);
        assertTrue(onFx(back::isDisabled));
        runFx(back::fire);
        assertFalse(left.get());

        gate.countDown();
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (onFx(back::isDisabled)) {
            assertTrue(System.nanoTime() < end, "驗證一直沒回來");
            Thread.sleep(50);
        }
        runFx(back::fire);
        assertTrue(left.get());
        runner.shutdownNow();
    }

    private static Button button(Node root, String text) {
        return root.lookupAll(".button").stream().map(Button.class::cast).filter(b -> b.getText().equals(text))
                .findFirst().orElseThrow();
    }

    private static <T> T onFx(Callable<T> work) throws Exception {
        FutureTask<T> task = new FutureTask<>(work);
        Platform.runLater(task);
        return task.get(30, TimeUnit.SECONDS);
    }

    private static void runFx(Runnable work) throws Exception {
        onFx(Executors.callable(work));
    }
}
