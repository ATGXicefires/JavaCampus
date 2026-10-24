package detective.ui;

import detective.content.CaseDef;
import detective.content.Check;
import detective.content.Evidence;
import detective.content.SetupVar;
import detective.content.Step;
import detective.engine.RunResult;
import detective.engine.RunResult.Outcome;
import detective.engine.RunResult.Probe;
import detective.engine.RunResult.Problem;
import detective.engine.RunResult.Severity;
import detective.engine.Verdict;
import detective.engine.Verifier;
import detective.fx.Motion;
import detective.fx.Sfx;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.TwoDimensional.Bias;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 調查畫面：左邊是任務，中間寫程式和看結果，右邊是證物與可用資料。 */
final class WorkspaceView extends StackPane {

    private final Game game;
    private final CaseDef c;
    private final CodeArea editor = new CodeArea();
    private final VBox stepList = new VBox(4);
    private final VBox detail = new VBox(12);
    private final VBox console = new VBox(10);
    private final ScrollPane consoleScroll = new ScrollPane(console);
    private final VBox evidenceBox = new VBox(18);
    private final Button runButton = new Button("▶  執行");
    private final Button verifyButton = new Button("提交推理");
    private final Button stopButton = new Button("停止");
    private final Button resetButton = new Button("重設");
    private final HintDrawer hints;
    private final PauseTransition autosave = new PauseTransition(Duration.seconds(1));
    // 大部分執行一下子就結束，等一會兒還沒跑完才露出「停止」，免得按鈕一閃而過
    private final PauseTransition stopReveal = new PauseTransition(Duration.millis(400));
    private final Set<String> shownEvidence = new HashSet<>();
    private Step current;
    private Step justSolved;
    private List<JavaHighlighter.Mark> marks = List.of();
    private int errorLine = -1;
    private Animation nudge;
    private boolean busy;

    WorkspaceView(Game game, CaseDef c) {
        this.game = game;
        this.c = c;
        hints = new HintDrawer(game, this::refreshSteps);
        BorderPane layout = new BorderPane();
        layout.setTop(topBar());
        layout.setLeft(leftPanel());
        layout.setCenter(centerPanel());
        layout.setRight(rightPanel());
        StackPane.setAlignment(hints, Pos.CENTER_RIGHT);
        getChildren().addAll(layout, hints);
        setupEditor();
        autosave.setOnFinished(e -> saveCode());
        stopReveal.setOnFinished(e -> stopButton.setVisible(busy));
        refreshEvidence(false);
        open(firstOpenStep());
    }

    private Node topBar() {
        Button back = new Button("← 案件列表");
        back.getStyleClass().add("btn-ghost");
        back.setOnAction(e -> leave(game::showCases));
        Label title = new Label("第 " + game.caseNumber(c) + " 案　" + c.title());
        title.getStyleClass().add("topbar-title");
        Label question = new Label(c.openQuestion());
        question.getStyleClass().add("open-question");
        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        Button hintButton = new Button("提示");
        hintButton.getStyleClass().add("btn-ghost");
        hintButton.setOnAction(e -> hints.toggle());
        HBox bar = new HBox(16, back, title, question, gap);
        if (game.isClosed(c)) {
            Button report = new Button("結案報告");
            report.getStyleClass().add("btn-ghost");
            report.setOnAction(e -> leave(() -> game.showVerdict(c)));
            bar.getChildren().add(report);
        }
        bar.getChildren().addAll(hintButton, game.settingsButton());
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("topbar");
        return bar;
    }

    private Node leftPanel() {
        ScrollPane detailScroll = new ScrollPane(detail);
        detailScroll.setFitToWidth(true);
        VBox.setVgrow(detailScroll, Priority.ALWAYS);
        detail.setPadding(new Insets(4, 6, 12, 2));
        VBox box = new VBox(10, stepList, new Separator(), detailScroll);
        box.setPadding(new Insets(14));
        box.setPrefWidth(330);
        box.getStyleClass().add("side-panel");
        return box;
    }

    private Node centerPanel() {
        VirtualizedScrollPane<CodeArea> editorScroll = new VirtualizedScrollPane<>(editor);
        runButton.getStyleClass().add("btn-primary");
        runButton.setTooltip(game.tooltip("Ctrl + Enter"));
        runButton.setOnAction(e -> run());
        verifyButton.getStyleClass().add("btn-verify");
        verifyButton.setTooltip(game.tooltip("Ctrl + Shift + Enter"));
        verifyButton.setOnAction(e -> verify());
        stopButton.getStyleClass().add("btn-ghost");
        stopButton.setOnAction(e -> game.sandbox.stop());
        stopButton.managedProperty().bind(stopButton.visibleProperty());
        stopButton.setVisible(false);
        resetButton.getStyleClass().add("btn-ghost");
        resetButton.setOnAction(e -> reset());
        HBox toolbar = new HBox(10, runButton, verifyButton, resetButton, stopButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("toolbar-row");

        VBox top = new VBox(editorScroll, toolbar);
        VBox.setVgrow(editorScroll, Priority.ALWAYS);
        console.getStyleClass().add("console");
        consoleScroll.setFitToWidth(true);
        consoleScroll.setFitToHeight(true);
        consoleScroll.getStyleClass().add("console-scroll");
        SplitPane split = new SplitPane(top, consoleScroll);
        split.setOrientation(Orientation.VERTICAL);
        split.setDividerPositions(0.6);
        return split;
    }

    private Node rightPanel() {
        evidenceBox.setPadding(new Insets(16));
        ScrollPane evidenceScroll = new ScrollPane(evidenceBox);
        evidenceScroll.setFitToWidth(true);
        Tab evidence = new Tab("證物", evidenceScroll);
        TabPane tabs = new TabPane(evidence);
        if (!c.vars().isEmpty()) {
            tabs.getTabs().add(new Tab("可用資料", dataPane()));
        }
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setPrefWidth(340);
        tabs.getStyleClass().add("side-panel");
        return tabs;
    }

    private Node dataPane() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        for (SetupVar v : c.vars()) {
            Label type = new Label(v.type());
            type.getStyleClass().add("var-type");
            Label name = new Label(v.name());
            name.getStyleClass().add("var-name");
            Label value = new Label(v.value());
            value.getStyleClass().add("var-value");
            VBox row = new VBox(4, new HBox(8, type, name), value);
            row.getStyleClass().add("hint-tier");
            box.getChildren().add(row);
        }
        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private void setupEditor() {
        editor.setParagraphGraphicFactory(this::lineNumber);
        editor.textProperty().addListener((obs, old, text) -> onEdited());
        editor.addEventFilter(KeyEvent.KEY_PRESSED, this::onKey);
        editor.addEventHandler(KeyEvent.KEY_TYPED, e -> {
            String ch = e.getCharacter();
            if (!ch.isEmpty() && !Character.isISOControl(ch.charAt(0)) && !e.isShortcutDown()) {
                game.sfx.play(Sfx.Sound.KEY);
            }
        });
    }

    private void onKey(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER && e.isShortcutDown()) {
            e.consume();
            if (e.isShiftDown()) {
                verify();
            } else {
                run();
            }
        } else if (e.getCode() == KeyCode.ENTER && !e.isShiftDown() && !e.isAltDown()) {
            e.consume();
            newlineWithIndent();
            game.sfx.play(Sfx.Sound.KEY);
        } else if (e.getCode() == KeyCode.TAB && !e.isShiftDown() && !e.isShortcutDown()) {
            e.consume();
            editor.replaceSelection("    ");
        }
    }

    private void newlineWithIndent() {
        String line = editor.getParagraph(editor.getCurrentParagraph()).getText();
        String before = line.substring(0, Math.min(editor.getCaretColumn(), line.length()));
        String indent = before.substring(0, before.length() - before.stripLeading().length());
        if (before.stripTrailing().endsWith("{")) {
            indent += "    ";
        }
        editor.replaceSelection("\n" + indent);
        editor.requestFollowCaret();
    }

    private Node lineNumber(int index) {
        Label l = new Label(String.format("%2d", index + 1));
        l.getStyleClass().add("lineno");
        if (index == errorLine) {
            l.getStyleClass().add("lineno-error");
        }
        return l;
    }

    private void setErrorLine(int line) {
        errorLine = line;
        // 換一個新的 factory 才會讓 RichTextFX 重畫行號
        editor.setParagraphGraphicFactory(null);
        editor.setParagraphGraphicFactory(this::lineNumber);
    }

    private void onEdited() {
        if (!marks.isEmpty() || errorLine >= 0) {
            marks = List.of();
            setErrorLine(-1);
        }
        highlight();
        stopNudge();
        autosave.playFromStart();
    }

    private void highlight() {
        editor.setStyleSpans(0, JavaHighlighter.spans(editor.getText(), marks));
    }

    private void saveCode() {
        if (current != null) {
            game.progress.setCode(c.id(), current.id(), editor.getText());
            game.save();
        }
    }

    private void leave(Runnable go) {
        autosave.stop();
        saveCode();
        go.run();
    }

    private Step firstOpenStep() {
        return c.steps().stream().filter(s -> !game.progress.isSolved(c.id(), s.id())).findFirst()
                .orElse(c.steps().getLast());
    }

    private void open(Step s) {
        if (current != null) {
            autosave.stop();
            saveCode();
        }
        current = s;
        marks = List.of();
        editor.replaceText(initialCode(s));
        editor.getUndoManager().forgetHistory();
        editor.moveTo(editor.getLength());
        setErrorLine(-1);
        highlight();
        refreshSteps();
        refreshDetail();
        hints.bind(c, s);
        console.getChildren().clear();
        // 畫面剛建立時編輯器還不在 scene 裡，要等加進去之後才拿得到焦點
        Platform.runLater(editor::requestFocus);
    }

    private String initialCode(Step s) {
        String saved = game.progress.code(c.id(), s.id());
        return saved != null ? saved : startingCode(s);
    }

    /** 沒有 starter 的步驟沿用上一步過關的程式，讓同一個案件的程式一路長大。 */
    private String startingCode(Step s) {
        if (!s.starter().isBlank()) {
            return s.starter();
        }
        int i = c.steps().indexOf(s);
        if (i > 0) {
            String previous = game.progress.code(c.id(), c.steps().get(i - 1).id());
            if (previous != null) {
                return previous;
            }
        }
        return "";
    }

    private void reset() {
        game.confirm("要把程式碼改回這一步一開始的樣子嗎？目前寫的內容會被取代。", "重設", () -> {
            editor.replaceText(startingCode(current));
            editor.moveTo(editor.getLength());
        });
    }

    private void refreshSteps() {
        stepList.getChildren().clear();
        int firstUnsolved = 0;
        while (firstUnsolved < c.steps().size() && game.progress.isSolved(c.id(), c.steps().get(firstUnsolved).id())) {
            firstUnsolved++;
        }
        for (int i = 0; i < c.steps().size(); i++) {
            Step s = c.steps().get(i);
            boolean solved = game.progress.isSolved(c.id(), s.id());
            boolean reachable = solved || i == firstUnsolved;
            Label num = new Label(solved ? "✓" : String.valueOf(i + 1));
            num.getStyleClass().add("step-num");
            Label title = new Label(s.title());
            title.getStyleClass().add("step-title");
            HBox row = new HBox(10, num, title);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("step-row");
            if (s == current) {
                row.getStyleClass().add("current");
            }
            if (solved) {
                row.getStyleClass().add("solved");
            }
            if (s == justSolved) {
                Motion.pop(num);
                justSolved = null;
            }
            if (!reachable) {
                row.getStyleClass().add("locked");
            } else {
                row.getStyleClass().add("pressable");
                row.setOnMouseClicked(e -> {
                    if (s != current && !busy) {
                        open(s);
                    }
                });
            }
            stepList.getChildren().add(row);
        }
    }

    private void refreshDetail() {
        Label title = new Label(current.title());
        title.getStyleClass().add("detail-title");
        title.setWrapText(true);
        Label shape = new Label(current.expectedShape());
        shape.getStyleClass().add("shape-box");
        shape.setMaxWidth(Double.MAX_VALUE);
        shape.setWrapText(true);
        detail.getChildren().setAll(title, MiniMarkdown.render(current.instructions()), shape);
        if (game.progress.isSolved(c.id(), current.id())) {
            VBox after = MiniMarkdown.render(current.afterText());
            after.getStyleClass().add("after-text");
            detail.getChildren().add(after);
        }
    }

    private void refreshEvidence(boolean animate) {
        evidenceBox.getChildren().clear();
        List<Evidence> visible = c.evidence().stream()
                .filter(e -> e.unlockOnStep() == null || game.progress.isSolved(c.id(), e.unlockOnStep()))
                .toList();
        boolean anyNew = false;
        for (Evidence ev : visible.reversed()) {
            boolean isNew = animate && !shownEvidence.contains(ev.id());
            Node card = evidenceCard(ev, isNew);
            evidenceBox.getChildren().add(card);
            if (isNew) {
                Motion.flipIn(card);
                anyNew = true;
            }
            shownEvidence.add(ev.id());
        }
        if (anyNew) {
            game.sfx.play(Sfx.Sound.UNLOCK);
        }
    }

    private static Node evidenceCard(Evidence ev, boolean isNew) {
        Label title = new Label(ev.title());
        title.getStyleClass().add("ev-title");
        HBox head = new HBox(8, title);
        head.setAlignment(Pos.CENTER_LEFT);
        if (isNew) {
            Label badge = new Label("新");
            badge.getStyleClass().add("ev-new");
            head.getChildren().add(badge);
        }
        VBox card = new VBox(8, head, MiniMarkdown.render(ev.content()));
        card.getStyleClass().addAll("paper", "evidence-card", ev.kind());
        card.setRotate((Math.floorMod(ev.id().hashCode(), 5) - 2) * 0.7);
        return card;
    }

    private void run() {
        if (busy) {
            return;
        }
        String code = editor.getText();
        Step step = current;
        List<String> probes = step.check().type() == Check.Mode.VALUES ? step.check().exprs() : List.of();
        setBusy(true);
        game.sfx.play(Sfx.Sound.RUN);
        game.background(() -> step.form() == Step.Form.PROGRAM
                ? game.sandbox.runProgram(code, true, probes)
                : game.sandbox.run(Verifier.preamble(c.vars(), Map.of()), code, probes), r -> {
            showRun(r);
            if (r.outcome() == Outcome.OK) {
                precheck(step, code);
            } else {
                game.sfx.play(Sfx.Sound.FAIL);
                Motion.shake(consoleScroll, 6);
            }
        }, () -> setBusy(false));
    }

    /** 執行成功後在背景先驗證一次，對了就讓「提交推理」閃動提醒。 */
    private void precheck(Step step, String code) {
        game.background(() -> game.verifier.verify(c.key(step), step.form(), c.vars(), step.check(), step.solution(), code), v -> {
            if (v.passed() && step == current && code.equals(editor.getText())
                    && !game.progress.isSolved(c.id(), step.id())) {
                startNudge();
            }
        }, () -> {});
    }

    private void verify() {
        if (busy) {
            return;
        }
        stopNudge();
        String code = editor.getText();
        Step step = current;
        setBusy(true);
        game.sfx.play(Sfx.Sound.RUN);
        game.background(() -> game.verifier.verify(c.key(step), step.form(), c.vars(), step.check(), step.solution(), code), v -> {
            showRun(v.run());
            if (v.passed()) {
                solved(step, code);
            } else {
                Node card = verdictCard(v);
                console.getChildren().addFirst(card);
                consoleScroll.setVvalue(0);
                game.sfx.play(Sfx.Sound.FAIL);
                Motion.shake(card, 8);
            }
        }, () -> setBusy(false));
    }

    private void solved(Step step, String code) {
        boolean firstTime = !game.progress.isSolved(c.id(), step.id());
        game.progress.markSolved(c.id(), step.id());
        game.progress.setCode(c.id(), step.id(), code);
        game.save();
        game.sfx.play(Sfx.Sound.SUCCESS);
        Motion.banner(this, "推理成立", "banner-ok");

        VBox card = new VBox(8, MiniMarkdown.inline(step.afterText()));
        card.getStyleClass().addAll("card", "success");
        int i = c.steps().indexOf(step);
        if (game.isClosed(c)) {
            Button close = new Button("結案　→");
            close.getStyleClass().add("btn-primary");
            close.setOnAction(e -> leave(() -> game.showVerdict(c)));
            card.getChildren().add(close);
        } else if (i + 1 < c.steps().size()) {
            Step next = c.steps().get(i + 1);
            Button go = new Button("下一步：" + next.title() + "　→");
            go.getStyleClass().add("btn-primary");
            go.setOnAction(e -> open(next));
            card.getChildren().add(go);
        }
        console.getChildren().addFirst(card);
        consoleScroll.setVvalue(0);
        if (firstTime) {
            justSolved = step;
        }
        refreshSteps();
        refreshDetail();
        if (firstTime) {
            PauseTransition later = new PauseTransition(Duration.millis(700));
            later.setOnFinished(e -> refreshEvidence(true));
            later.play();
        }
    }

    private void showRun(RunResult r) {
        List<Node> nodes = new ArrayList<>();
        for (Problem p : r.problems()) {
            nodes.add(problemCard(p, r.outcome()));
        }
        if (r.output().isEmpty()) {
            if (r.outcome() == Outcome.OK && r.probes().isEmpty()) {
                Label empty = new Label("（沒有輸出）");
                empty.getStyleClass().add("console-empty");
                nodes.add(empty);
            }
        } else {
            if (!nodes.isEmpty()) {
                nodes.add(new Separator());
            }
            Label out = new Label(r.output().stripTrailing());
            out.getStyleClass().add("console-out");
            out.setWrapText(true);
            nodes.add(out);
            if (r.truncated()) {
                nodes.add(muted("……輸出太多，只顯示前面一部分。"));
            }
        }
        if (!r.probes().isEmpty()) {
            if (!nodes.isEmpty()) {
                nodes.add(new Separator());
            }
            for (Probe p : r.probes()) {
                Label line = new Label(p.error() == null
                        ? p.expr() + " = " + p.value() + "　（" + p.type() + "）"
                        : p.expr() + "：（還沒有這個變數）");
                line.getStyleClass().add("probe-line");
                nodes.add(line);
            }
        }
        console.getChildren().setAll(nodes);
        consoleScroll.setVvalue(0);

        marks = r.problems().stream()
                .map(p -> new JavaHighlighter.Mark(p.start(), p.end(), p.severity() == Severity.ERROR ? "err" : "warn"))
                .toList();
        setErrorLine(r.firstError().map(p -> lineOf(p.start())).orElse(-1));
        highlight();
    }

    private Node problemCard(Problem p, Outcome outcome) {
        boolean warning = p.severity() == Severity.WARNING;
        String title = warning ? "提醒" : switch (outcome) {
            case COMPILE_ERROR -> "編譯錯誤";
            case RUNTIME_ERROR -> "執行錯誤";
            case TIMEOUT -> "執行太久";
            case STOPPED -> "已停止";
            case EXITED -> "執行環境重新啟動";
            case OK -> "錯誤";
        };
        Label head = new Label(title + "　第 " + (lineOf(p.start()) + 1) + " 行");
        head.getStyleClass().add("card-title");
        Label raw = new Label(p.raw());
        raw.getStyleClass().add("raw");
        raw.setWrapText(true);
        raw.setVisible(false);
        raw.setManaged(false);
        Hyperlink toggle = new Hyperlink("原文");
        toggle.setOnAction(e -> {
            boolean show = !raw.isVisible();
            raw.setVisible(show);
            raw.setManaged(show);
            toggle.setText(show ? "收起" : "原文");
        });
        VBox card = new VBox(6, head, MiniMarkdown.inline(p.message()), toggle, raw);
        card.getStyleClass().addAll("card", warning ? "warning" : "error");
        card.setOnMouseClicked(e -> {
            editor.moveTo(Math.min(p.start(), editor.getLength()));
            editor.requestFollowCaret();
            editor.requestFocus();
        });
        return card;
    }

    private Node verdictCard(Verdict v) {
        Label head = new Label("推理不成立");
        head.getStyleClass().add("card-title");
        VBox card = new VBox(6, head, MiniMarkdown.inline(v.message()));
        card.getStyleClass().addAll("card", "error");
        return card;
    }

    private int lineOf(int offset) {
        return editor.offsetToPosition(Math.min(offset, editor.getLength()), Bias.Forward).getMajor();
    }

    private void setBusy(boolean b) {
        busy = b;
        runButton.setDisable(b);
        verifyButton.setDisable(b);
        resetButton.setDisable(b);
        if (b) {
            stopReveal.playFromStart();
        } else {
            stopReveal.stop();
            stopButton.setVisible(false);
        }
    }

    private void startNudge() {
        if (nudge != null) {
            return;
        }
        verifyButton.getStyleClass().add("ready");
        nudge = Motion.pulse(verifyButton);
    }

    private void stopNudge() {
        if (nudge != null) {
            nudge.stop();
            nudge = null;
        }
        verifyButton.getStyleClass().remove("ready");
    }

    private static Label muted(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("muted");
        l.setWrapText(true);
        return l;
    }
}
