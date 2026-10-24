package campus.ui;

import campus.engine.JavaTokens;
import campus.engine.JavaTokens.Kind;
import campus.engine.JavaTokens.Token;
import campus.lesson.SetupVar;
import javafx.application.Platform;
import javafx.event.EventTarget;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Popup;
import org.fxmisc.richtext.CodeArea;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 編輯器的自動完成，用起來跟一般 IDE 一樣：打字時在游標下面列出關鍵字、常用寫法和程式裡已經有的名字，
 * ↑↓ 選、Enter 或 Tab 填入、Esc 關掉，Ctrl + Space 手動叫出來。選到的那一項在清單下面附一句白話說明。
 */
final class CodeCompletion {

    /**
     * @param insert 填入的文字；{@code §} 是填完之後游標停的位置，換行會補上目前這一行的縮排
     * @param alias  另一種叫出它的打法，例如 {@code sout}
     */
    record Item(String label, String insert, String note, String alias) {
        Item(String label, String insert, String note) {
            this(label, insert, note, null);
        }

        boolean matches(String prefix) {
            String p = prefix.toLowerCase(Locale.ROOT);
            return label.toLowerCase(Locale.ROOT).startsWith(p) || alias != null && alias.startsWith(p);
        }
    }

    private static final int MAX_ROWS = 8;

    private static final List<Item> KEYWORDS = List.of(
            new Item("System.out.println", "System.out.println(§);", "印出一行，印完換行", "sout"),
            new Item("System.out.print", "System.out.print(§);", "印出來，但不換行"),
            new Item("int", "int ", "裝整數的盒子"),
            new Item("double", "double ", "裝小數的盒子"),
            new Item("boolean", "boolean ", "裝是非的盒子：true 或 false"),
            new Item("char", "char ", "裝一個字元的盒子，用單引號"),
            new Item("long", "long ", "裝很大的整數"),
            new Item("String", "String ", "裝文字的盒子，文字用雙引號"),
            new Item("if", "if (§) {\n    \n}", "如果條件成立，就做大括號裡的事"),
            new Item("else", "else {\n    §\n}", "否則：if 的條件不成立時做這裡"),
            new Item("else if", "else if (§) {\n    \n}", "否則如果：再檢查另一個條件"),
            new Item("for", "for (int i = 0; i < §; i++) {\n    \n}", "重複做固定的次數"),
            new Item("while", "while (§) {\n    \n}", "條件成立就一直重複"),
            new Item("return", "return ", "把結果交回去，方法到這裡結束"),
            new Item("true", "true", "是"),
            new Item("false", "false", "否"),
            new Item("null", "null", "什麼都沒有"),
            new Item("break", "break;", "直接跳出迴圈"),
            new Item("continue", "continue;", "跳過這一輪，直接進下一輪"),
            new Item("new", "new ", "做出一個新的物件"),
            new Item("final", "final ", "放進去之後就不能再改"),
            new Item("Math", "Math", "數學工具：取大小、次方、亂數"),
            new Item("Integer", "Integer", "整數工具：把文字變成整數"));

    /** 只有完整程式才用得到，寫片段的時候不列出來。 */
    private static final List<Item> PROGRAM_KEYWORDS = List.of(
            new Item("main", "public static void main(String[] args) {\n    §\n}", "程式從這裡開始執行", "psvm"),
            new Item("class", "class ", "定義一種新的東西（類別）"),
            new Item("public", "public ", "公開：別的類別也能用"),
            new Item("private", "private ", "私人：只有自己這個類別能用"),
            new Item("static", "static ", "屬於類別本身，不用先做出物件"),
            new Item("void", "void ", "這個方法做完事，不交回任何結果"),
            new Item("this", "this", "這個物件自己"),
            new Item("import", "import ", "把別的套件裡的類別拿進來用"));

    private static final Map<String, List<Item>> MEMBERS = Map.of(
            "System", List.of(
                    new Item("out", "out", "標準輸出：印到畫面上")),
            "System.out", List.of(
                    new Item("println", "println(§);", "印出一行，印完換行"),
                    new Item("print", "print(§);", "印出來，但不換行"),
                    new Item("printf", "printf(§);", "照格式印出來")),
            "Math", List.of(
                    new Item("max", "max(§)", "兩個數取比較大的"),
                    new Item("min", "min(§)", "兩個數取比較小的"),
                    new Item("abs", "abs(§)", "絕對值"),
                    new Item("pow", "pow(§)", "次方，例如 pow(2, 3) 是 8"),
                    new Item("sqrt", "sqrt(§)", "平方根"),
                    new Item("round", "round(§)", "四捨五入"),
                    new Item("random", "random()", "0 到 1 之間的亂數"),
                    new Item("PI", "PI", "圓周率")),
            "Integer", List.of(
                    new Item("parseInt", "parseInt(§)", "把文字變成整數，例如 \"7\" 變成 7"),
                    new Item("MAX_VALUE", "MAX_VALUE", "int 能裝的最大值")),
            "String", List.of(
                    new Item("valueOf", "valueOf(§)", "把東西變成文字"),
                    new Item("format", "format(§)", "照格式組出一段文字")));

    private static final List<Item> STRING_MEMBERS = List.of(
            new Item("length", "length()", "有幾個字"),
            new Item("charAt", "charAt(§)", "第幾個字（從 0 開始數）"),
            new Item("equals", "equals(§)", "內容是不是一樣；比較文字要用它，不要用 =="),
            new Item("isEmpty", "isEmpty()", "是不是一個字都沒有"),
            new Item("contains", "contains(§)", "有沒有包含某段文字"),
            new Item("substring", "substring(§)", "切出其中一段"),
            new Item("indexOf", "indexOf(§)", "某段文字在第幾個位置"),
            new Item("startsWith", "startsWith(§)", "是不是用某段文字開頭"),
            new Item("endsWith", "endsWith(§)", "是不是用某段文字結尾"),
            new Item("replace", "replace(§)", "把某些字換掉"),
            new Item("trim", "trim()", "去掉前後的空白"),
            new Item("toUpperCase", "toUpperCase()", "英文轉大寫"),
            new Item("toLowerCase", "toLowerCase()", "英文轉小寫"));

    private static final Pattern DECLARED = Pattern.compile("\\b(String|int|double|boolean|char|long)\\s+([A-Za-z_]\\w*)\\s*[=;,)]");
    private static final Pattern CREATED = Pattern.compile("\\b([A-Z]\\w*)\\s+([A-Za-z_]\\w*)\\s*=\\s*new\\b");
    private static final Pattern CLASS = Pattern.compile("\\b(?:class|record|enum|interface)\\s+([A-Z]\\w*)");
    private static final String MODIFIERS = "(?:(?:public|private|protected|static|final)\\s+)*";
    private static final Pattern METHOD = Pattern.compile("^\\s*" + MODIFIERS + "[\\w<>\\[\\],]+\\s+(\\w+)\\s*\\(([^)]*)\\)");
    private static final Pattern FIELD = Pattern.compile("^\\s*" + MODIFIERS + "([\\w<>\\[\\]]+)\\s+(\\w+)\\s*[=;]");
    private static final Set<String> NOT_TYPES = Set.of("return", "new", "else", "throw");

    private final Game game;
    private final CodeArea area;
    private final Supplier<String> allCode;
    private final List<SetupVar> vars;
    private final boolean program;
    private final Popup popup = new Popup();
    private final VBox rows = new VBox();
    private final Label note = new Label();
    private final VBox box = new VBox(rows, note);
    private final Scale scale = new Scale();
    private List<Item> shown = List.of();
    private int selected;
    private int wordStart;

    /**
     * @param allCode 這一步所有檔案的程式碼，分頁裡別的檔案宣告的名字也要能補
     * @param vars    題目預先放好的變數
     * @param program 寫的是完整程式，才列出 main、class 這些
     */
    private CodeCompletion(Game game, CodeArea area, Supplier<String> allCode, List<SetupVar> vars, boolean program) {
        this.game = game;
        this.area = area;
        this.allCode = allCode;
        this.vars = vars;
        this.program = program;
        rows.getStyleClass().add("completion-rows");
        note.getStyleClass().add("completion-note");
        note.setWrapText(true);
        note.setMaxWidth(Double.MAX_VALUE);
        box.getStyleClass().add("completion");
        box.setPrefWidth(340);
        box.getTransforms().add(scale);
        popup.getContent().add(new Group(box));
        popup.setAutoHide(true);
        // 點編輯器別的地方收起清單時，那一下還是要能移動游標
        popup.setConsumeAutoHidingEvents(false);
        // Esc 由 Game 統一處理，這裡讓它先問自動完成要不要收起來，才不會一按就打開設定
        popup.setHideOnEscape(false);
        area.getProperties().put(CodeCompletion.class, this);
    }

    /** 掛到一個編輯器上。按鍵的過濾要比其他人早掛，填入用的 Enter、Tab 才不會先被換行、縮排吃掉。 */
    static CodeCompletion attach(Game game, CodeArea area, Supplier<String> allCode, List<SetupVar> vars,
                                 boolean program) {
        CodeCompletion c = new CodeCompletion(game, area, allCode, vars, program);
        area.addEventFilter(KeyEvent.KEY_PRESSED, c::onKeyPressed);
        area.addEventHandler(KeyEvent.KEY_TYPED, c::onKeyTyped);
        area.textProperty().addListener((obs, old, now) -> c.refreshIfShowing());
        area.caretPositionProperty().addListener((obs, old, now) -> c.refreshIfShowing());
        area.focusedProperty().addListener((obs, was, now) -> {
            if (!now) {
                c.hide();
            }
        });
        area.sceneProperty().addListener((obs, was, now) -> c.hide());
        return c;
    }

    /** Esc 落在有自動完成開著的編輯器上：收起清單並回傳 true，這一下 Esc 就不再做別的事。 */
    static boolean dismiss(EventTarget target) {
        if (target instanceof CodeArea a && a.getProperties().get(CodeCompletion.class) instanceof CodeCompletion c
                && c.popup.isShowing()) {
            c.hide();
            return true;
        }
        return false;
    }

    private void onKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.SPACE && e.isShortcutDown()) {
            e.consume();
            update(true);
            return;
        }
        if (!popup.isShowing()) {
            return;
        }
        switch (e.getCode()) {
            case UP -> select(selected - 1);
            case DOWN -> select(selected + 1);
            case ENTER, TAB -> accept(shown.get(selected));
            default -> {
                return;
            }
        }
        e.consume();
    }

    /** 打出名字的一部分或「.」才自動跳出來；貼上、重設這類整段換掉的變動不會。 */
    private void onKeyTyped(KeyEvent e) {
        String ch = e.getCharacter();
        if (ch.isEmpty() || e.isShortcutDown() || e.isAltDown()) {
            return;
        }
        char c = ch.charAt(0);
        // 已經開著的話，文字一變就會更新，不用這裡再算一次
        if (!popup.isShowing() && (c == '.' || Character.isJavaIdentifierPart(c) && !Character.isISOControl(c))) {
            // 這時候字還沒進編輯器，等它進去再算
            Platform.runLater(() -> update(false));
        }
    }

    private void refreshIfShowing() {
        if (popup.isShowing()) {
            Platform.runLater(() -> update(false));
        }
    }

    private void update(boolean forced) {
        String text = area.getText();
        int caret = area.getCaretPosition();
        int start = caret;
        while (start > 0 && Character.isJavaIdentifierPart(text.charAt(start - 1))) {
            start--;
        }
        String prefix = text.substring(start, caret);
        String qualifier = qualifier(text, start);
        boolean number = !prefix.isEmpty() && Character.isDigit(prefix.charAt(0));
        if (number || inLiteral(text, caret) || prefix.isEmpty() && qualifier == null && !forced) {
            hide();
            return;
        }
        List<Item> matches = suggestions(qualifier, prefix);
        // 已經打完整個字、清單裡只剩它自己的時候，不用再跳出來擋路
        if (matches.isEmpty() || matches.size() == 1 && matches.getFirst().label().equals(prefix)) {
            hide();
            return;
        }
        wordStart = start;
        shown = matches;
        rows.getChildren().clear();
        String family = "-fx-font-family: \"" + game.progress.settings().codeFamily() + "\";";
        for (int i = 0; i < shown.size(); i++) {
            int index = i;
            Label row = new Label(shown.get(i).label());
            row.getStyleClass().add("completion-item");
            row.setStyle(family);
            row.setMaxWidth(Double.MAX_VALUE);
            row.setOnMouseEntered(e -> select(index));
            row.setOnMouseClicked(e -> accept(shown.get(index)));
            rows.getChildren().add(row);
        }
        box.getStyleClass().remove("dark");
        if (game.progress.settings().codeDark()) {
            box.getStyleClass().add("dark");
        }
        select(0);
        show(start == caret ? area.getCaretBounds() : area.getCharacterBoundsOnScreen(start, start + 1));
    }

    private void show(Optional<Bounds> at) {
        if (at.isEmpty()) {
            hide();
            return;
        }
        double f = game.uiScale().get();
        scale.setX(f);
        scale.setY(f);
        Bounds b = at.get();
        if (popup.isShowing()) {
            popup.setAnchorX(b.getMinX());
            popup.setAnchorY(b.getMaxY() + 2);
        } else {
            popup.show(area, b.getMinX(), b.getMaxY() + 2);
        }
    }

    void hide() {
        popup.hide();
    }

    private void select(int i) {
        if (shown.isEmpty()) {
            return;
        }
        selected = Math.floorMod(i, shown.size());
        for (int r = 0; r < rows.getChildren().size(); r++) {
            rows.getChildren().get(r).getStyleClass().remove("selected");
        }
        rows.getChildren().get(selected).getStyleClass().add("selected");
        note.setText(shown.get(selected).note());
    }

    private void accept(Item item) {
        hide();
        String text = area.getText();
        int lineStart = text.lastIndexOf('\n', wordStart - 1) + 1;
        int indentEnd = lineStart;
        while (indentEnd < text.length() && (text.charAt(indentEnd) == ' ' || text.charAt(indentEnd) == '\t')) {
            indentEnd++;
        }
        String insert = item.insert().replace("\n", "\n" + text.substring(lineStart, Math.min(indentEnd, wordStart)));
        int cursor = insert.indexOf('§');
        insert = insert.replace("§", "");
        area.replaceText(wordStart, area.getCaretPosition(), insert);
        area.moveTo(wordStart + (cursor < 0 ? insert.length() : cursor));
        area.requestFollowCaret();
    }

    /** 名字前面如果是「.」，往回找出點號前面的那一串，例如 {@code System.out}；後面不是點號就是 null。 */
    static String qualifier(String text, int wordStart) {
        if (wordStart == 0 || text.charAt(wordStart - 1) != '.') {
            return null;
        }
        int i = wordStart - 1;
        while (i > 0 && (Character.isJavaIdentifierPart(text.charAt(i - 1)) || text.charAt(i - 1) == '.')) {
            i--;
        }
        return text.substring(i, wordStart - 1);
    }

    /** 游標在字串、字元或註解裡面時不要跳出來，那裡寫的是中文或說明，不是程式。 */
    static boolean inLiteral(String text, int caret) {
        for (Token t : JavaTokens.tokenize(text.substring(0, caret))) {
            if ((t.kind() == Kind.STRING || t.kind() == Kind.CHAR || t.kind() == Kind.COMMENT) && t.end() == caret) {
                boolean closed = t.text().length() > 1 && t.text().endsWith(t.text().substring(0, 1))
                        && t.kind() != Kind.COMMENT;
                boolean blockClosed = t.text().startsWith("/*") && t.text().endsWith("*/") && t.text().length() > 3;
                return !closed && !blockClosed;
            }
        }
        return false;
    }

    private List<Item> suggestions(String qualifier, String prefix) {
        String code = allCode.get();
        Map<String, String> types = declaredTypes(code);
        List<Item> pool;
        if (qualifier != null) {
            pool = members(code, types, qualifier);
        } else {
            Map<String, Item> byLabel = new LinkedHashMap<>();
            types.forEach((name, type) -> byLabel.putIfAbsent(name, new Item(name, name, "變數，裝的是 " + type)));
            for (SetupVar v : vars) {
                byLabel.putIfAbsent(v.name(), new Item(v.name(), v.name(), "題目給的資料，裝的是 " + v.type()));
            }
            KEYWORDS.forEach(k -> byLabel.putIfAbsent(k.label(), k));
            if (program) {
                PROGRAM_KEYWORDS.forEach(k -> byLabel.putIfAbsent(k.label(), k));
            }
            Matcher cls = CLASS.matcher(code);
            while (cls.find()) {
                byLabel.putIfAbsent(cls.group(1), new Item(cls.group(1), cls.group(1), "你寫的類別"));
            }
            // 只收程式裡的名字，字串和註解裡的英文不算；正在打的這個字本身也不算
            for (Token t : JavaTokens.tokenize(code)) {
                if (t.kind() == Kind.IDENT && t.text().length() > 1 && !t.text().equals(prefix)) {
                    byLabel.putIfAbsent(t.text(), new Item(t.text(), t.text(), "程式裡出現過的名字"));
                }
            }
            pool = new ArrayList<>(byLabel.values());
        }
        String lower = prefix.toLowerCase(Locale.ROOT);
        return pool.stream()
                .filter(it -> it.matches(prefix))
                // 大小寫完全對上的排前面，其餘照原本的順序：變數、關鍵字、你的類別、其他名字
                .sorted(Comparator.comparingInt(it -> it.label().startsWith(prefix) ? 0
                        : it.label().toLowerCase(Locale.ROOT).startsWith(lower) ? 1 : 2))
                .limit(MAX_ROWS)
                .toList();
    }

    private List<Item> members(String code, Map<String, String> types, String qualifier) {
        List<Item> known = MEMBERS.get(qualifier);
        if (known != null) {
            return known;
        }
        String type = types.get(qualifier);
        if (type == null) {
            type = createdTypes(code).get(qualifier);
        }
        if ("String".equals(type)) {
            return STRING_MEMBERS;
        }
        if (type != null) {
            return classMembers(code, type);
        }
        return Character.isUpperCase(qualifier.charAt(0)) ? classMembers(code, qualifier) : List.of();
    }

    private Map<String, String> declaredTypes(String code) {
        Map<String, String> types = new LinkedHashMap<>();
        for (SetupVar v : vars) {
            types.put(v.name(), v.type());
        }
        Matcher m = DECLARED.matcher(code);
        while (m.find()) {
            types.putIfAbsent(m.group(2), m.group(1));
        }
        return types;
    }

    private static Map<String, String> createdTypes(String code) {
        Map<String, String> types = new LinkedHashMap<>();
        Matcher m = CREATED.matcher(code);
        while (m.find()) {
            types.putIfAbsent(m.group(2), m.group(1));
        }
        return types;
    }

    /** 從玩家自己寫的類別裡找出方法和欄位；只看類別最外層，方法裡面的區域變數不算。 */
    static List<Item> classMembers(String code, String className) {
        Matcher head = Pattern.compile("\\b(?:class|record|enum|interface)\\s+" + Pattern.quote(className) + "\\b").matcher(code);
        if (!head.find()) {
            return List.of();
        }
        int open = code.indexOf('{', head.end());
        if (open < 0) {
            return List.of();
        }
        List<Item> found = new ArrayList<>();
        int depth = 0;
        int lineStart = open + 1;
        for (int i = open + 1; i < code.length() && depth >= 0; i++) {
            char c = code.charAt(i);
            if (c == '\n' || c == '{' || c == '}' || i == code.length() - 1) {
                if (depth == 0) {
                    memberOn(code.substring(lineStart, i), className, found);
                }
                lineStart = i + 1;
            }
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
            }
        }
        return found;
    }

    private static void memberOn(String line, String className, List<Item> found) {
        Matcher m = METHOD.matcher(line);
        if (m.find() && !m.group(1).equals(className)) {
            String insert = m.group(2).isBlank() ? m.group(1) + "()" : m.group(1) + "(§)";
            found.add(new Item(m.group(1), insert, className + " 的方法"));
            return;
        }
        Matcher f = FIELD.matcher(line);
        if (f.find() && !NOT_TYPES.contains(f.group(1))) {
            found.add(new Item(f.group(2), f.group(2), className + " 的欄位，裝的是 " + f.group(1)));
        }
    }
}
