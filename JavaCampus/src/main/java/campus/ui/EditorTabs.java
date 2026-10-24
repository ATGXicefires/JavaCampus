package campus.ui;

import campus.engine.JavaTokens;
import campus.engine.RunResult.Problem;
import campus.engine.RunResult.Severity;
import campus.fx.Motion;
import campus.fx.Sfx;
import campus.lesson.SetupVar;
import campus.lesson.SourceFile;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.TwoDimensional.Bias;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 程式題的編輯區：上面一排檔案分頁，下面是目前那個檔案的編輯器。每個檔案各用一個 CodeArea，
 * 切換分頁時文字、游標、捲動位置和復原紀錄都各自留著。
 */
final class EditorTabs extends VBox {

    private final Game game;
    private final List<SetupVar> vars;
    private final Runnable onEdited;
    private final Runnable onFilesChanged;
    private final HBox bar = new HBox();
    private final Button addButton = new Button("＋");
    private final StackPane pages = new StackPane();
    private final List<FileTab> tabs = new ArrayList<>();
    private FileTab selected;
    private Predicate<String> removable = name -> false;
    private boolean program;

    /**
     * @param vars           題目預先放好的變數，自動完成會列出來
     * @param onEdited       任何一個檔案的內容被改了
     * @param onFilesChanged 新增或刪除了檔案，檔案清單要馬上存起來
     */
    EditorTabs(Game game, List<SetupVar> vars, Runnable onEdited, Runnable onFilesChanged) {
        this.game = game;
        this.vars = vars;
        this.onEdited = onEdited;
        this.onFilesChanged = onFilesChanged;
        getStyleClass().add("editor-tabs");
        bar.getStyleClass().add("file-tabs");
        bar.setAlignment(Pos.CENTER_LEFT);
        addButton.getStyleClass().add("file-add");
        addButton.setTooltip(game.tooltip("新增檔案"));
        addButton.setOnAction(e -> promptNewFile());
        addButton.managedProperty().bind(addButton.visibleProperty());
        VBox.setVgrow(pages, Priority.ALWAYS);
        getChildren().addAll(bar, pages);
    }

    /**
     * 換成另一個步驟的檔案，分頁和復原紀錄都重新開始。
     *
     * @param canAdd    能不能新增檔案（完整程式才可以）
     * @param removable 哪些檔案是玩家自己加的，可以刪掉
     */
    void open(List<SourceFile> files, boolean canAdd, Predicate<String> removable) {
        this.removable = removable;
        program = canAdd;
        tabs.clear();
        pages.getChildren().clear();
        bar.getChildren().setAll(addButton);
        selected = null;
        files.forEach(this::addTab);
        addButton.setVisible(canAdd);
        select(tabs.getFirst());
    }

    /** 改回 {@code files}：同名的檔案直接換掉內容，還能復原；多出來的分頁拿掉，少的補上。 */
    void revert(List<SourceFile> files) {
        List<String> names = files.stream().map(SourceFile::name).toList();
        for (FileTab t : List.copyOf(tabs)) {
            if (!names.contains(t.name)) {
                removeTab(t);
            }
        }
        for (SourceFile f : files) {
            FileTab t = tab(f.name());
            if (t == null) {
                addTab(f);
            } else {
                t.area.replaceText(f.text());
                t.area.moveTo(t.area.getLength());
            }
        }
        if (selected == null) {
            select(tabs.getFirst());
        }
        onFilesChanged.run();
    }

    /** 每個分頁的程式碼接在一起，別的檔案宣告的類別和方法也要能補。 */
    private String allCode() {
        return String.join("\n", tabs.stream().map(t -> t.area.getText()).toList());
    }

    List<SourceFile> files() {
        return tabs.stream().map(t -> new SourceFile(t.name, t.area.getText())).toList();
    }

    /** 每個問題標在它所在的檔案裡；片段的問題沒有檔名，就是唯一的那個分頁。 */
    void showProblems(List<Problem> problems) {
        for (FileTab t : tabs) {
            List<Problem> mine = problems.stream().filter(p -> tabFor(p.file()) == t).toList();
            t.marks = mine.stream()
                    .map(p -> new JavaHighlighter.Mark(p.start(), p.end(), p.severity() == Severity.ERROR ? "err" : "warn"))
                    .toList();
            t.setErrorLine(mine.stream().filter(p -> p.severity() == Severity.ERROR).findFirst()
                    .map(p -> t.lineOf(p.start())).orElse(-1));
            t.highlight();
        }
    }

    int lineOf(String file, int offset) {
        return tabFor(file).lineOf(offset);
    }

    void jumpTo(String file, int offset) {
        FileTab t = tabFor(file);
        select(t);
        t.area.moveTo(Math.min(offset, t.area.getLength()));
        t.area.requestFollowCaret();
        t.area.requestFocus();
    }

    void focusEditor() {
        selected.area.requestFocus();
    }

    private FileTab tab(String name) {
        return tabs.stream().filter(t -> t.name.equals(name)).findFirst().orElse(null);
    }

    private FileTab tabFor(String file) {
        FileTab t = file == null ? null : tab(file);
        return t != null ? t : tabs.getFirst();
    }

    private FileTab addTab(SourceFile f) {
        FileTab t = new FileTab(f.name(), f.text());
        tabs.add(t);
        pages.getChildren().add(t.scroll);
        bar.getChildren().add(bar.getChildren().size() - 1, t.head);
        return t;
    }

    private void removeTab(FileTab t) {
        int i = tabs.indexOf(t);
        tabs.remove(t);
        pages.getChildren().remove(t.scroll);
        bar.getChildren().remove(t.head);
        if (selected == t) {
            selected = null;
            if (!tabs.isEmpty()) {
                select(tabs.get(Math.max(0, i - 1)));
            }
        }
    }

    private void select(FileTab t) {
        if (selected != null) {
            selected.scroll.setVisible(false);
            selected.head.getStyleClass().remove("selected");
        }
        selected = t;
        t.scroll.setVisible(true);
        t.head.getStyleClass().add("selected");
    }

    private void promptNewFile() {
        Label question = new Label("新的類別要叫什麼名字？");
        TextField field = new TextField();
        field.setPromptText("Student");
        field.getStyleClass().add("code-font");
        HBox.setHgrow(field, Priority.ALWAYS);
        Label suffix = new Label(".java");
        suffix.getStyleClass().add("code-font");
        HBox input = new HBox(6, field, suffix);
        input.setAlignment(Pos.CENTER_LEFT);
        Label error = new Label();
        error.getStyleClass().add("new-file-error");
        error.setWrapText(true);
        error.managedProperty().bind(error.visibleProperty());
        error.setVisible(false);
        Button cancel = new Button("取消");
        cancel.getStyleClass().add("btn-ghost");
        cancel.setOnAction(e -> game.closeOverlay());
        Button create = new Button("建立");
        create.getStyleClass().add("btn-primary");
        create.disableProperty().bind(field.textProperty().isEmpty());
        Runnable submit = () -> {
            String name = field.getText().strip().replaceFirst("\\.java$", "");
            String problem = nameProblem(name);
            if (problem != null) {
                error.setText(problem);
                error.setVisible(true);
                Motion.shake(field, 6);
                return;
            }
            game.closeOverlay();
            addClass(name);
        };
        create.setOnAction(e -> submit.run());
        field.setOnAction(e -> submit.run());
        HBox buttons = new HBox(10, cancel, create);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        VBox panel = new VBox(14, question, input, error, buttons);
        panel.getStyleClass().add("overlay-panel");
        panel.setPrefWidth(440);
        panel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        game.openOverlay(panel, this::focusEditor);
        Platform.runLater(field::requestFocus);
    }

    /** 檔名就是類別名，要是合法的 Java 名字，也不能跟現有的檔案重複。 */
    private String nameProblem(String name) {
        if (name.isEmpty() || !Character.isJavaIdentifierStart(name.codePointAt(0))
                || !name.codePoints().allMatch(Character::isJavaIdentifierPart)) {
            return "類別的名字要用字母開頭，後面只能接字母、數字或底線。";
        }
        if (JavaTokens.KEYWORDS.contains(name)) {
            return "「" + name + "」是 Java 保留的字，不能當類別的名字。";
        }
        if (tab(name + ".java") != null) {
            return "已經有 " + name + ".java 了。";
        }
        return null;
    }

    private void addClass(String name) {
        String text = "public class " + name + " {\n    \n}\n";
        FileTab t = addTab(new SourceFile(name + ".java", text));
        select(t);
        t.area.moveTo(text.indexOf('\n') + 5);
        Motion.pop(t.head);
        onFilesChanged.run();
    }

    private void confirmRemove(FileTab t) {
        game.confirm("要刪除 " + t.name + " 嗎？裡面的程式碼會一起刪掉。", "刪除", () -> {
            removeTab(t);
            onFilesChanged.run();
        });
    }

    /** 一個檔案：分頁標籤、編輯器，以及這次執行標出來的錯誤。 */
    private final class FileTab {
        final String name;
        final CodeArea area = new CodeArea();
        final VirtualizedScrollPane<CodeArea> scroll = new VirtualizedScrollPane<>(area);
        final HBox head = new HBox(6);
        List<JavaHighlighter.Mark> marks = List.of();
        int errorLine = -1;

        FileTab(String name, String text) {
            this.name = name;
            area.setParagraphGraphicFactory(this::lineNumber);
            area.replaceText(text);
            area.getUndoManager().forgetHistory();
            area.moveTo(area.getLength());
            highlight();
            // 載入時的內容不算玩家改的，listener 要等放好文字之後才掛上
            area.textProperty().addListener((obs, old, now) -> edited());
            // 自動完成要先掛，清單開著時 Enter、Tab 是填入，不是換行和縮排
            CodeCompletion.attach(game, area, EditorTabs.this::allCode, vars, program);
            area.addEventFilter(KeyEvent.KEY_PRESSED, this::onKey);
            area.addEventHandler(KeyEvent.KEY_TYPED, e -> {
                String ch = e.getCharacter();
                if (!ch.isEmpty() && !Character.isISOControl(ch.charAt(0)) && !e.isShortcutDown()) {
                    game.sfx.play(Sfx.Sound.KEY);
                }
            });
            scroll.setVisible(false);

            head.getChildren().add(new Label(name));
            if (removable.test(name)) {
                Button close = new Button("✕");
                close.getStyleClass().add("file-close");
                close.setOnAction(e -> confirmRemove(this));
                head.getChildren().add(close);
            }
            head.setAlignment(Pos.CENTER_LEFT);
            head.getStyleClass().addAll("file-tab", "pressable");
            head.setOnMouseClicked(e -> {
                select(this);
                area.requestFocus();
            });
        }

        int lineOf(int offset) {
            return area.offsetToPosition(Math.min(offset, area.getLength()), Bias.Forward).getMajor();
        }

        void highlight() {
            area.setStyleSpans(0, JavaHighlighter.spans(area.getText(), marks));
        }

        void setErrorLine(int line) {
            errorLine = line;
            // 換一個新的 factory 才會讓 RichTextFX 重畫行號
            area.setParagraphGraphicFactory(null);
            area.setParagraphGraphicFactory(this::lineNumber);
            head.getStyleClass().remove("has-error");
            if (line >= 0) {
                head.getStyleClass().add("has-error");
            }
        }

        private void edited() {
            if (!marks.isEmpty() || errorLine >= 0) {
                marks = List.of();
                setErrorLine(-1);
            }
            highlight();
            onEdited.run();
        }

        private Node lineNumber(int index) {
            Label l = new Label(String.format("%2d", index + 1));
            l.getStyleClass().add("lineno");
            if (index == errorLine) {
                l.getStyleClass().add("lineno-error");
            }
            return l;
        }

        private void onKey(KeyEvent e) {
            if (e.isConsumed()) {
                return;
            }
            if (e.getCode() == KeyCode.ENTER && !e.isShortcutDown() && !e.isShiftDown() && !e.isAltDown()) {
                e.consume();
                newlineWithIndent();
                game.sfx.play(Sfx.Sound.KEY);
            } else if (e.getCode() == KeyCode.TAB && !e.isShiftDown() && !e.isShortcutDown()) {
                e.consume();
                area.replaceSelection("    ");
            }
        }

        private void newlineWithIndent() {
            String line = area.getParagraph(area.getCurrentParagraph()).getText();
            String before = line.substring(0, Math.min(area.getCaretColumn(), line.length()));
            String indent = before.substring(0, before.length() - before.stripLeading().length());
            if (before.stripTrailing().endsWith("{")) {
                indent += "    ";
            }
            area.replaceSelection("\n" + indent);
            area.requestFollowCaret();
        }
    }
}
