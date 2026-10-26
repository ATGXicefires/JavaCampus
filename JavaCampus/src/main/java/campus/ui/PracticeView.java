package campus.ui;

import campus.fx.Motion;
import campus.lesson.Lesson;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/** 練習模式：終端機風格，打指令或點清單挑一題來寫。指令的邏輯在 {@link PracticeShell}，這裡只負責畫和收輸入。 */
final class PracticeView extends StackPane {

    /** 輸出一行接一行冒出來的間隔。 */
    private static final Duration PER_LINE = Duration.millis(20);
    private static final double MAX_WIDTH = 1240;

    private final Game game;
    private final PracticeShell shell;
    private final Label title = new Label();
    private final VBox output = new VBox();
    private final TextFlow prompt = new TextFlow();
    private final TextField input = new TextField();
    private final Deque<Node> pending = new ArrayDeque<>();
    private final Timeline reveal = new Timeline(new KeyFrame(PER_LINE, e -> showNext()));

    PracticeView(Game game) {
        this.game = game;
        shell = new PracticeShell(game.story.contents(), game.lessons.lessons().values(),
                game.progress.storyLessons(), game.progress.practiceLessons());
        getStyleClass().add("practice");

        title.getStyleClass().add("term-title");
        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        Button close = new Button("✕");
        close.getStyleClass().add("term-close");
        close.setFocusTraversable(false);
        close.setOnAction(e -> game.showTitle());
        HBox bar = new HBox(title, gap, close);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("term-titlebar");

        input.getStyleClass().add("term-input");
        input.addEventFilter(KeyEvent.KEY_PRESSED, this::onKey);
        HBox.setHgrow(input, Priority.ALWAYS);
        HBox inputLine = new HBox(prompt, input);
        inputLine.setAlignment(Pos.BASELINE_LEFT);
        VBox body = new VBox(output, inputLine);
        body.getStyleClass().add("term");
        // 捲動區一按下就搶焦點，焦點回到輸入列時，已經打的字會被全選、接著打字就蓋掉了；
        // 點清單上的章或題目、點空白處之後，都要能直接接著打字
        ScrollPane scroll = new ScrollPane(body) {
            @Override
            public void requestFocus() {
            }
        };
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("term-scroll");
        // 點畫面上任何地方都是要打字
        scroll.setOnMouseClicked(e -> input.requestFocus());
        // 輸出變長就捲到最下面，跟終端機一樣
        body.heightProperty().addListener((obs, old, h) -> scroll.setVvalue(scroll.getVmax()));
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox window = new VBox(bar, scroll);
        window.getStyleClass().add("term-window");
        window.setMaxWidth(MAX_WIDTH);
        getChildren().add(window);
        setPadding(new Insets(40));

        reveal.setCycleCount(Animation.INDEFINITE);
        refreshPrompt();
        if (Motion.isOff()) {
            submit(shell.execute("ls"));
        } else {
            // 等畫面換進來一些再開始印，才看得到一行行冒出來
            PauseTransition wait = new PauseTransition(Duration.millis(250));
            wait.setOnFinished(e -> submit(shell.execute("ls")));
            wait.play();
        }
        // 畫面剛建立時還不在 scene 裡，要等加進去之後才拿得到焦點
        Platform.runLater(input::requestFocus);
    }

    /** 從題目回來：寫完了補印一行打勾，再接著打字。 */
    void returnFrom(Lesson lesson) {
        print(shell.back(lesson));
        Platform.runLater(input::requestFocus);
    }

    private void onKey(KeyEvent e) {
        switch (e.getCode()) {
            case ENTER -> {
                String text = input.getText();
                input.clear();
                submit(shell.execute(text));
            }
            case UP -> setInput(shell.older(input.getText()));
            case DOWN -> setInput(shell.newer(input.getText()));
            case TAB -> complete();
            default -> {
                return;
            }
        }
        e.consume();
    }

    private void complete() {
        String text = input.getText();
        PracticeShell.Completion c = shell.complete(text);
        setInput(c.input());
        if (!c.options().isEmpty()) {
            print(shell.optionLines(text, c.options()));
        }
    }

    private void setInput(String text) {
        input.setText(text);
        input.end();
    }

    private void submit(PracticeShell.Result r) {
        switch (r.action()) {
            case NONE -> print(r.lines());
            case CLEAR -> clear();
            case EXIT -> game.showTitle();
            case RUN -> {
                print(r.lines());
                flush();
                game.showPracticeLesson(r.lesson(), this);
            }
        }
        refreshPrompt();
    }

    private void refreshPrompt() {
        prompt.getChildren().setAll(shell.prompt().stream().map(PracticeView::text).toList());
        title.setText(shell.title());
    }

    private void print(List<PracticeShell.Line> lines) {
        for (PracticeShell.Line l : lines) {
            pending.add(render(l));
        }
        if (Motion.isOff()) {
            flush();
        } else if (reveal.getStatus() != Animation.Status.RUNNING) {
            showNext();
            reveal.play();
        }
    }

    private void showNext() {
        Node line = pending.poll();
        if (line == null) {
            reveal.stop();
            return;
        }
        output.getChildren().add(line);
        Motion.slideFrom(line, -12);
    }

    private void flush() {
        reveal.stop();
        while (!pending.isEmpty()) {
            output.getChildren().add(pending.poll());
        }
    }

    private void clear() {
        reveal.stop();
        pending.clear();
        output.getChildren().clear();
    }

    private Node render(PracticeShell.Line line) {
        TextFlow flow = new TextFlow();
        flow.getChildren().setAll(line.spans().stream().map(PracticeView::text).toList());
        flow.getStyleClass().add("term-line");
        if (line.link() != null) {
            flow.getStyleClass().add("term-link");
            // 只有字的範圍能點，滑過時的底色也只蓋到字，不會整行都亮
            flow.setMaxWidth(Region.USE_PREF_SIZE);
            flow.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    e.consume();
                    submit(shell.click(line.link()));
                    input.requestFocus();
                }
            });
        }
        return flow;
    }

    private static Text text(PracticeShell.Span span) {
        Text t = new Text(span.text());
        t.getStyleClass().add("term-" + span.style().name().toLowerCase(Locale.ROOT));
        return t;
    }
}
