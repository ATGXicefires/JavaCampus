package campus.engine;

import campus.engine.JavaTokens.Kind;
import campus.engine.JavaTokens.Token;
import campus.engine.RunResult.Outcome;
import campus.engine.RunResult.Probe;
import campus.engine.RunResult.Problem;
import campus.engine.RunResult.Severity;
import campus.lesson.SourceFile;
import jdk.jshell.DeclarationSnippet;
import jdk.jshell.Diag;
import jdk.jshell.EvalException;
import jdk.jshell.ExpressionSnippet;
import jdk.jshell.ImportSnippet;
import jdk.jshell.JShell;
import jdk.jshell.JShellException;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis.CompletionInfo;
import jdk.jshell.SourceCodeAnalysis.Completeness;
import jdk.jshell.TypeDeclSnippet;
import jdk.jshell.VarSnippet;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 在獨立的 JVM 裡執行玩家的程式碼，並把 JShell 比真正 Java 寬鬆的地方補回來：
 * 一律要求分號、有編譯錯誤就整段不執行、不能重複宣告同名變數。
 */
public final class Sandbox implements AutoCloseable {

    private static final int OUTPUT_LIMIT = 64 * 1024;
    // 完整程式從 Main 類別開始執行，只有一個檔案時那個檔案就是 Main.java
    private static final String MAIN_CLASS = "Main";

    /** @param file 所在的檔案；片段沒有檔名，是 null。{@code start} 相對於那個檔案 */
    private record Segment(String file, int start, String text, Completeness completeness) {
        int end() {
            return start + text.length();
        }
    }

    /** 完整程式裡的一個檔案，連同切好的段落。 */
    private record Unit(SourceFile file, String code, List<Segment> segments) {}

    private final Duration timeout;
    private final CappedOutput output = new CappedOutput(OUTPUT_LIMIT);
    private final PrintStream printStream = new PrintStream(output, true, UTF_8);
    private final ScheduledExecutorService watchdog = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "sandbox-watchdog");
        t.setDaemon(true);
        return t;
    });
    private volatile JShell shell;
    private volatile boolean exited;
    private volatile Outcome interruption;

    public Sandbox(Duration timeout) {
        this.timeout = timeout;
    }

    /**
     * 從乾淨的狀態執行一次：先宣告 {@code preamble}，再執行 {@code source}，成功的話對每個 {@code probes} 求值。
     *
     * @throws IllegalStateException preamble 本身無法執行，代表案件內容有誤
     */
    public synchronized RunResult run(List<String> preamble, String source, List<String> probes) {
        JShell js = cleanShell();
        Set<String> given = new HashSet<>();
        for (String line : preamble) {
            for (SnippetEvent ev : js.eval(line)) {
                if (ev.causeSnippet() == null && (ev.status() != Status.VALID || ev.exception() != null)) {
                    throw new IllegalStateException("題目設定無法執行：" + line);
                }
                if (ev.snippet() instanceof VarSnippet v) {
                    given.add(v.name());
                }
            }
        }

        List<Segment> segments = split(js, source, null);
        Optional<Problem> early = conflictMarker(source, null).or(() -> missingSemicolon(segments));
        if (early.isPresent()) {
            return compileError(List.of(early.get()));
        }

        output.reset();
        interruption = null;
        exited = false;
        Set<String> declared = new HashSet<>(given);
        Map<String, Segment> bySnippetId = new HashMap<>();
        Segment current = null;
        ScheduledFuture<?> dog = arm(js);
        try {
            for (Segment seg : segments) {
                current = seg;
                List<SnippetEvent> events;
                try {
                    events = js.eval(seg.text());
                } catch (IllegalStateException closed) {
                    return finish(Outcome.EXITED, List.of(errorAt(seg, Diagnostics.exited(), "JShell closed")));
                }
                if (exited) {
                    return finish(Outcome.EXITED, List.of(errorAt(seg, Diagnostics.exited(), "System.exit")));
                }
                if (interruption != null) {
                    return interrupted(seg);
                }
                for (SnippetEvent ev : events) {
                    if (ev.causeSnippet() != null) {
                        continue;
                    }
                    Snippet sn = ev.snippet();
                    bySnippetId.put(sn.id(), seg);
                    if (ev.status() == Status.REJECTED) {
                        return compileError(rejected(js, sn, seg, source));
                    }
                    if (ev.status() == Status.RECOVERABLE_DEFINED || ev.status() == Status.RECOVERABLE_NOT_DEFINED) {
                        if (sn instanceof DeclarationSnippet ds) {
                            return compileError(unresolved(js, ds, seg, source));
                        }
                    }
                    if (sn instanceof VarSnippet v && v.subKind() != Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND
                            && !declared.add(v.name())) {
                        return compileError(List.of(errorAt(seg,
                                Diagnostics.alreadyDefined(v.name(), given.contains(v.name())),
                                "variable " + v.name() + " is already defined")));
                    }
                    if (ev.exception() != null) {
                        return finish(Outcome.RUNTIME_ERROR, List.of(runtimeProblem(ev.exception(), bySnippetId, seg)));
                    }
                }
            }
        } finally {
            dog.cancel(false);
        }
        if (interruption != null && current != null) {
            return interrupted(current);
        }
        String text = output.text();
        boolean truncated = output.truncated();
        List<Probe> probeResults = evaluate(js, probes);
        return new RunResult(Outcome.OK, text, truncated, Lint.check(source, stringVariables(js)), probeResults);
    }

    /** 只有一個檔案的完整程式，當成 {@code Main.java}。 */
    public RunResult runProgram(String source, boolean launch, List<String> probes) {
        return runProgram(List.of(new SourceFile(MAIN_CLASS + ".java", source)), launch, probes);
    }

    /**
     * 把 {@code files} 當成同一個專案裡的幾個 {@code .java} 檔，從乾淨的狀態一起執行：每個檔案只能有 import 和型別宣告，
     * import 只對自己的檔案有效，類別可以互相使用，不會注入任何案件變數。類別都定義好之後，{@code launch} 為 true 時像
     * {@code java Main} 一樣呼叫 {@code Main.main}，最後對每個 {@code probes} 求值（例如用 {@code Main.leaveTime(22, 20, 25)}
     * 當隱藏測資）。每個問題都會標出在哪個檔案，位置相對於那個檔案。
     */
    public synchronized RunResult runProgram(List<SourceFile> files, boolean launch, List<String> probes) {
        JShell js = cleanShell();
        List<Unit> units = new ArrayList<>();
        for (SourceFile f : files) {
            String code = blankPackage(f.text());
            units.add(new Unit(f, code, split(js, code, f.name())));
        }
        Optional<Problem> early = units.stream()
                .map(u -> conflictMarker(u.code(), u.file().name()).or(() -> missingSemicolon(u.segments())))
                .flatMap(Optional::stream).findFirst().or(() -> structure(js, units)).or(() -> foreignImport(js, units));
        if (early.isPresent()) {
            return compileError(List.of(early.get()));
        }

        output.reset();
        interruption = null;
        exited = false;
        Map<String, Segment> bySnippetId = new HashMap<>();
        Map<DeclarationSnippet, Segment> declared = new LinkedHashMap<>();
        ScheduledFuture<?> dog = arm(js);
        try {
            for (Unit u : units) {
                for (Segment seg : u.segments()) {
                    List<SnippetEvent> events;
                    try {
                        events = js.eval(seg.text());
                    } catch (IllegalStateException closed) {
                        return finish(Outcome.EXITED, List.of(errorAt(seg, Diagnostics.exited(), "JShell closed")));
                    }
                    for (SnippetEvent ev : events) {
                        if (ev.causeSnippet() != null) {
                            continue;
                        }
                        bySnippetId.put(ev.snippet().id(), seg);
                        if (ev.status() == Status.REJECTED) {
                            return compileError(rejected(js, ev.snippet(), seg, u.code()));
                        }
                        if (ev.snippet() instanceof DeclarationSnippet ds) {
                            declared.put(ds, seg);
                        }
                    }
                }
            }
            // 類別可以用到後面才宣告的類別，所以全部定義完才檢查還有沒有找不到的名字
            for (Map.Entry<DeclarationSnippet, Segment> d : declared.entrySet()) {
                if (js.status(d.getKey()) != Status.VALID) {
                    Segment seg = d.getValue();
                    String code = units.stream().filter(u -> u.segments().contains(seg)).findFirst().orElseThrow().code();
                    return compileError(unresolved(js, d.getKey(), seg, code));
                }
            }
            if (launch) {
                Optional<RunResult> failed = launch(js, units, bySnippetId);
                if (failed.isPresent()) {
                    return failed.get();
                }
            }
        } finally {
            dog.cancel(false);
        }
        String text = output.text();
        boolean truncated = output.truncated();
        List<Probe> probeResults = evaluate(js, probes);
        List<Problem> warnings = new ArrayList<>();
        for (Unit u : units) {
            for (Problem p : Lint.check(u.file().text(), Set.of())) {
                warnings.add(new Problem(p.severity(), p.start(), p.end(), p.message(), p.raw(), u.file().name()));
            }
        }
        return new RunResult(Outcome.OK, text, truncated, warnings, probeResults);
    }

    /** 呼叫 {@code Main.main}；正常結束時回傳空的 Optional。 */
    private Optional<RunResult> launch(JShell js, List<Unit> units, Map<String, Segment> bySnippetId) {
        Segment main = units.stream().flatMap(u -> u.segments().stream())
                .filter(s -> MAIN_CLASS.equals(typeName(js, s))).findFirst().orElse(null);
        if (main == null) {
            Unit unit = units.getFirst();
            Segment first = unit.segments().isEmpty()
                    ? new Segment(unit.file().name(), 0, "", Completeness.EMPTY) : unit.segments().getFirst();
            return Optional.of(compileError(List.of(header(first, Diagnostics.noMainClass(MAIN_CLASS),
                    "error: could not find or load main class " + MAIN_CLASS))));
        }
        List<SnippetEvent> events;
        try {
            events = js.eval(MAIN_CLASS + ".main(new String[0]);");
        } catch (IllegalStateException closed) {
            return Optional.of(finish(Outcome.EXITED, List.of(header(main, Diagnostics.exited(), "JShell closed"))));
        }
        if (exited) {
            return Optional.of(finish(Outcome.EXITED, List.of(header(main, Diagnostics.exited(), "System.exit"))));
        }
        if (interruption != null) {
            String message = interruption == Outcome.TIMEOUT ? Diagnostics.timeout(timeout) : Diagnostics.stopped();
            return Optional.of(finish(interruption, List.of(header(main, message, interruption.name()))));
        }
        SnippetEvent ev = events.stream().filter(e -> e.causeSnippet() == null).findFirst().orElseThrow();
        if (ev.status() == Status.REJECTED) {
            String raw = js.diagnostics(ev.snippet()).filter(Diag::isError).findFirst()
                    .map(d -> Diagnostics.cleanWrapperNames(d.getMessage(Locale.ENGLISH))).orElse("main not found");
            return Optional.of(compileError(List.of(header(main, Diagnostics.noMainMethod(MAIN_CLASS), raw))));
        }
        if (ev.exception() != null) {
            return Optional.of(finish(Outcome.RUNTIME_ERROR, List.of(runtimeProblem(ev.exception(), bySnippetId, main))));
        }
        return Optional.empty();
    }

    /** 真正的 Java 檔案裡，class 外面只能有 import；public 類別的名字要跟檔名一樣；整個專案的類別不能重複宣告。 */
    private static Optional<Problem> structure(JShell js, List<Unit> units) {
        Set<String> names = new HashSet<>();
        for (Unit u : units) {
            boolean seenType = false;
            for (Segment seg : u.segments()) {
                List<Snippet> parsed = js.sourceCodeAnalysis().sourceToSnippets(seg.text());
                if (parsed.isEmpty()) {
                    continue;
                }
                Snippet sn = parsed.getFirst();
                switch (sn.kind()) {
                    case IMPORT -> {
                        if (seenType) {
                            return Optional.of(header(seg, Diagnostics.importAfterType(),
                                    "class, interface, enum, or record expected"));
                        }
                    }
                    case TYPE_DECL -> {
                        seenType = true;
                        String name = ((TypeDeclSnippet) sn).name();
                        if (!names.add(name)) {
                            return Optional.of(header(seg, Diagnostics.duplicateType(name), "duplicate class: " + name));
                        }
                        String file = u.file().baseName();
                        if (isPublic(seg) && !name.equals(file)) {
                            return Optional.of(header(seg, Diagnostics.publicClassName(name, file),
                                    "class " + name + " is public, should be declared in a file named " + name + ".java"));
                        }
                    }
                    // 語法本身有錯，交給編譯器回報真正的原因
                    case ERRONEOUS -> { }
                    default -> {
                        return Optional.of(header(seg, Diagnostics.outsideClass(),
                                "class, interface, enum, or record expected"));
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * JShell 的 import 對之後所有的程式碼都有效，真正的 Java 卻只管到寫它的那個檔案。
     * 某個檔案用到的類別只有別的檔案 import 過時要擋下來，不然搬到真正的專案就編譯不過。
     */
    private static Optional<Problem> foreignImport(JShell js, List<Unit> units) {
        if (units.size() < 2) {
            return Optional.empty();
        }
        Set<String> types = new HashSet<>();
        List<Set<String>> imports = new ArrayList<>();
        for (Unit u : units) {
            Set<String> own = new HashSet<>();
            for (Segment seg : u.segments()) {
                for (Snippet sn : js.sourceCodeAnalysis().sourceToSnippets(seg.text())) {
                    if (sn instanceof ImportSnippet im && !im.isStatic()) {
                        own.add(im.fullname());
                    } else if (sn instanceof TypeDeclSnippet t) {
                        types.add(t.name());
                    }
                }
            }
            imports.add(own);
        }
        for (int i = 0; i < units.size(); i++) {
            Unit u = units.get(i);
            List<Token> code = JavaTokens.tokenize(u.code()).stream().filter(t -> t.kind() != Kind.COMMENT).toList();
            for (int k = 0; k < code.size(); k++) {
                Token t = code.get(k);
                String pkg = Diagnostics.packageOf(t.text());
                // 前面接著「.」的是寫了完整路徑的名字，例如 java.util.List，不需要 import
                if (t.kind() != Kind.IDENT || pkg == null || types.contains(t.text())
                        || k > 0 && code.get(k - 1).text().equals(".") || importsType(imports.get(i), pkg, t.text())) {
                    continue;
                }
                for (int j = 0; j < units.size(); j++) {
                    if (j != i && importsType(imports.get(j), pkg, t.text())) {
                        String english = "cannot find symbol\n  symbol:   class " + t.text() + "\n  location: class";
                        return Optional.of(new Problem(Severity.ERROR, t.start(), t.end(),
                                Diagnostics.compileError("compiler.err.cant.resolve.location", english,
                                        new Diagnostics.Names(Set.of(), Set.of(), types)),
                                "cannot find symbol: class " + t.text(), u.file().name()));
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static boolean importsType(Set<String> imported, String pkg, String type) {
        return imported.contains(pkg + "." + type) || imported.contains(pkg + ".*");
    }

    private static String typeName(JShell js, Segment seg) {
        return js.sourceCodeAnalysis().sourceToSnippets(seg.text()).stream()
                .filter(s -> s instanceof TypeDeclSnippet).map(s -> ((TypeDeclSnippet) s).name())
                .findFirst().orElse(null);
    }

    private static boolean isPublic(Segment seg) {
        for (Token t : JavaTokens.tokenize(seg.text())) {
            if (t.kind() == Kind.COMMENT) {
                continue;
            }
            if (Set.of("class", "interface", "enum", "record").contains(t.text())) {
                return false;
            }
            if (t.text().equals("public")) {
                return true;
            }
        }
        return false;
    }

    /** JShell 不認得 {@code package}，單一檔案的程式有沒有它也沒差別，換成等長的空白讓位置維持不變。 */
    private static String blankPackage(String source) {
        List<Token> code = JavaTokens.tokenize(source).stream().filter(t -> t.kind() != Kind.COMMENT).toList();
        if (code.isEmpty() || !code.getFirst().text().equals("package")) {
            return source;
        }
        int end = code.stream().filter(t -> t.text().equals(";")).mapToInt(Token::end).findFirst().orElse(-1);
        if (end < 0) {
            return source;
        }
        StringBuilder sb = new StringBuilder(source);
        for (int i = code.getFirst().start(); i < end; i++) {
            if (sb.charAt(i) != '\n') {
                sb.setCharAt(i, ' ');
            }
        }
        return sb.toString();
    }

    /** 結構上的錯誤只標出那一段的第一行，整個 class 都畫紅線反而看不出重點。 */
    private static Problem header(Segment seg, String message, String raw) {
        String text = seg.text();
        int start = JavaTokens.tokenize(text).stream().filter(t -> t.kind() != Kind.COMMENT)
                .mapToInt(Token::start).findFirst().orElse(0);
        int nl = text.indexOf('\n', start);
        int end = nl < 0 ? text.stripTrailing().length() : nl;
        return new Problem(Severity.ERROR, seg.start() + start, seg.start() + Math.max(end, start + 1), message, raw,
                seg.file());
    }

    /** 可從其他執行緒呼叫，中斷正在執行的程式。 */
    public void stop() {
        interruption = Outcome.STOPPED;
        JShell js = shell;
        if (js != null) {
            js.stop();
        }
    }

    @Override
    public synchronized void close() {
        watchdog.shutdownNow();
        JShell js = shell;
        shell = null;
        if (js != null) {
            js.close();
        }
    }

    private JShell ensureShell() {
        JShell js = shell;
        if (js != null && !exited) {
            return js;
        }
        if (js != null) {
            js.close();
        }
        JShell fresh = JShell.builder()
                .in(InputStream.nullInputStream())
                .out(printStream)
                .err(printStream)
                .remoteVMOptions("-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8", "-Xmx256m")
                .build();
        // 關掉舊的 JShell 也會觸發 shutdown，只理會目前這一個
        fresh.onShutdown(s -> {
            if (s == shell) {
                exited = true;
            }
        });
        shell = fresh;
        exited = false;
        return fresh;
    }

    /**
     * 拿到一個乾淨的 JShell。平常 drop 掉上次的 snippet 就好，但上次若定義過型別就整個重開：
     * JShell 有個 bug，型別宣告被 drop 之後，參考型別運算式的型別名稱會變成 {@code java.lang.Object}，
     * 比對變數型別時就會誤判。重開一個只要零點一秒多。
     */
    private JShell cleanShell() {
        JShell js = ensureShell();
        if (js.types().findAny().isEmpty()) {
            reset(js);
            return js;
        }
        shell = null;
        js.close();
        return ensureShell();
    }

    private static void reset(JShell js) {
        List<Snippet> active = js.snippets().filter(s -> js.status(s).isActive()).toList();
        active.forEach(js::drop);
    }

    private ScheduledFuture<?> arm(JShell js) {
        return watchdog.schedule(() -> {
            interruption = Outcome.TIMEOUT;
            js.stop();
        }, timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    private static List<Segment> split(JShell js, String source, String file) {
        List<Segment> out = new ArrayList<>();
        int offset = 0;
        String rest = source;
        while (!rest.isBlank()) {
            CompletionInfo ci = js.sourceCodeAnalysis().analyzeCompletion(rest);
            if (ci.completeness() == Completeness.EMPTY) {
                break;
            }
            // COMPLETE_WITH_SEMI 時 source() 會被補上分號，原文要從 remaining() 反推
            String text = ci.source() == null ? rest : rest.substring(0, rest.length() - ci.remaining().length());
            if (text.isEmpty()) {
                break;
            }
            out.add(new Segment(file, offset, text, ci.completeness()));
            if (ci.source() == null) {
                break;
            }
            offset += text.length();
            rest = ci.remaining();
        }
        return out;
    }

    /** 合併衝突的標記不是 Java，編譯器只會說運算式在這裡斷掉了；直接指出是哪一行。 */
    private static Optional<Problem> conflictMarker(String source, String file) {
        int at = 0;
        for (String line : source.split("\n", -1)) {
            if (line.startsWith("<<<<<<<") || line.startsWith("=======") || line.startsWith(">>>>>>>")) {
                return Optional.of(new Problem(Severity.ERROR, at, at + line.stripTrailing().length(),
                        Diagnostics.conflictMarker(), "merge conflict marker", file));
            }
            at += line.length() + 1;
        }
        return Optional.empty();
    }

    /** JShell 會自動補分號、也接受結尾沒有分號的運算式，真正的 Java 兩者都不允許。 */
    private static Optional<Problem> missingSemicolon(List<Segment> segments) {
        for (Segment seg : segments) {
            if (seg.completeness() != Completeness.COMPLETE && seg.completeness() != Completeness.COMPLETE_WITH_SEMI) {
                continue;
            }
            List<Token> code = JavaTokens.tokenize(seg.text()).stream().filter(t -> t.kind() != Kind.COMMENT).toList();
            if (code.isEmpty()) {
                continue;
            }
            Token last = code.getLast();
            if (seg.completeness() == Completeness.COMPLETE_WITH_SEMI || !last.text().equals(";") && !last.text().equals("}")) {
                return Optional.of(new Problem(Severity.ERROR, seg.start() + last.start(), seg.start() + last.end(),
                        Diagnostics.missingSemicolon(), "';' expected", seg.file()));
            }
        }
        return Optional.empty();
    }

    private List<Problem> rejected(JShell js, Snippet sn, Segment seg, String source) {
        Diagnostics.Names names = names(js);
        List<Problem> out = new ArrayList<>();
        js.diagnostics(sn).filter(Diag::isError).forEach(d -> {
            String english = d.getMessage(Locale.ENGLISH);
            int start = d.getStartPosition() < 0 ? 0 : (int) d.getStartPosition();
            int end = d.getEndPosition() < 0 ? seg.text().length() : (int) d.getEndPosition();
            start = Math.min(seg.start() + start, source.length());
            end = Math.min(Math.max(seg.start() + end, start + 1), source.length());
            out.add(new Problem(Severity.ERROR, start, end,
                    Diagnostics.compileError(d.getCode(), english, names), Diagnostics.cleanWrapperNames(english),
                    seg.file()));
        });
        if (out.isEmpty()) {
            out.add(errorAt(seg, "這一段程式沒辦法編譯。", "rejected"));
        }
        return out;
    }

    private List<Problem> unresolved(JShell js, DeclarationSnippet ds, Segment seg, String source) {
        Diagnostics.Names names = names(js);
        List<Problem> out = js.unresolvedDependencies(ds).map(dep -> {
            String english = "cannot find symbol\n  symbol:   " + dep + "\n  location: class";
            String name = dep.replaceFirst("^\\w+\\s+", "").replaceFirst("\\(.*", "");
            int idx = seg.text().indexOf(name);
            int start = idx < 0 ? seg.start() : seg.start() + idx;
            int end = idx < 0 ? seg.end() : start + name.length();
            return new Problem(Severity.ERROR, start, end,
                    Diagnostics.compileError("compiler.err.cant.resolve.location", english, names), "cannot find symbol: " + dep,
                    seg.file());
        }).toList();
        // 子類別沒把參數交給父類別的建構子，JShell 也當成之後還能補救，但其實沒有缺少的名字，要看它的錯誤
        return out.isEmpty() ? rejected(js, ds, seg, source) : out;
    }

    private static Problem runtimeProblem(JShellException ex, Map<String, Segment> bySnippetId, Segment current) {
        String cls = ex instanceof EvalException ee ? ee.getExceptionClassName() : ex.getClass().getName();
        Segment seg = current;
        int line = -1;
        for (StackTraceElement el : ex.getStackTrace()) {
            String file = el.getFileName();
            if (file != null && file.startsWith("#") && bySnippetId.containsKey(file.substring(1))) {
                seg = bySnippetId.get(file.substring(1));
                line = el.getLineNumber();
                break;
            }
        }
        int[] range = lineRange(seg, line);
        String raw = cls + (ex.getMessage() == null ? "" : ": " + Diagnostics.cleanWrapperNames(ex.getMessage()));
        return new Problem(Severity.ERROR, range[0], range[1], Diagnostics.runtimeError(cls, ex.getMessage()), raw,
                seg.file());
    }

    /**
     * JShell 堆疊的行號是相對於 snippet 去掉開頭空白之後的內容，註解行也算在內。
     * 沒有行號時，標出開頭註解之後的程式碼。
     */
    private static int[] lineRange(Segment seg, int line) {
        String text = seg.text();
        int lead = 0;
        while (lead < text.length() && Character.isWhitespace(text.charAt(lead))) {
            lead++;
        }
        if (line >= 1) {
            int from = lead;
            for (int i = 1; i < line && from >= 0; i++) {
                int nl = text.indexOf('\n', from);
                from = nl < 0 ? -1 : nl + 1;
            }
            if (from >= 0) {
                int to = text.indexOf('\n', from);
                to = to < 0 ? text.length() : to;
                String content = text.substring(from, to);
                if (!content.isBlank()) {
                    int indent = content.length() - content.stripLeading().length();
                    return new int[] {seg.start() + from + indent, seg.start() + from + content.stripTrailing().length()};
                }
            }
        }
        int codeStart = JavaTokens.tokenize(text).stream().filter(t -> t.kind() != Kind.COMMENT)
                .mapToInt(Token::start).findFirst().orElse(lead);
        return new int[] {seg.start() + codeStart, seg.start() + text.stripTrailing().length()};
    }

    private List<Probe> evaluate(JShell js, List<String> probes) {
        List<Probe> out = new ArrayList<>();
        if (probes.isEmpty()) {
            return out;
        }
        interruption = null;
        ScheduledFuture<?> dog = arm(js);
        try {
            for (String expr : probes) {
                out.add(probe(js, expr));
            }
        } finally {
            dog.cancel(false);
            output.reset();
        }
        return out;
    }

    private Probe probe(JShell js, String expr) {
        List<SnippetEvent> events;
        try {
            events = js.eval(expr);
        } catch (IllegalStateException closed) {
            return new Probe(expr, null, null, Diagnostics.exited());
        }
        if (interruption == Outcome.TIMEOUT) {
            return new Probe(expr, null, null, Diagnostics.timeout(timeout));
        }
        SnippetEvent ev = events.stream().filter(e -> e.causeSnippet() == null).findFirst().orElse(null);
        if (ev == null) {
            return new Probe(expr, null, null, "沒辦法求值。");
        }
        if (ev.status() == Status.REJECTED) {
            Diagnostics.Names names = names(js);
            String error = js.diagnostics(ev.snippet()).filter(Diag::isError).findFirst()
                    .map(d -> Diagnostics.compileError(d.getCode(), d.getMessage(Locale.ENGLISH), names))
                    .orElse("沒辦法求值。");
            return new Probe(expr, null, null, error);
        }
        if (ev.exception() != null) {
            String cls = ev.exception() instanceof EvalException ee ? ee.getExceptionClassName() : ev.exception().getClass().getName();
            return new Probe(expr, null, null, Diagnostics.runtimeError(cls, ev.exception().getMessage()));
        }
        String type = switch (ev.snippet()) {
            case VarSnippet v -> v.typeName();
            case ExpressionSnippet e -> e.typeName();
            default -> "void";
        };
        return new Probe(expr, type, ev.value(), null);
    }

    private RunResult interrupted(Segment seg) {
        Outcome outcome = interruption;
        String message = outcome == Outcome.TIMEOUT ? Diagnostics.timeout(timeout) : Diagnostics.stopped();
        return finish(outcome, List.of(errorAt(seg, message, outcome.name())));
    }

    private RunResult finish(Outcome outcome, List<Problem> problems) {
        return new RunResult(outcome, output.text(), output.truncated(), problems, List.of());
    }

    private static RunResult compileError(List<Problem> problems) {
        return new RunResult(Outcome.COMPILE_ERROR, "", false, problems, List.of());
    }

    private static Problem errorAt(Segment seg, String message, String raw) {
        int[] range = lineRange(seg, -1);
        return new Problem(Severity.ERROR, range[0], range[1], message, raw, seg.file());
    }

    private static Diagnostics.Names names(JShell js) {
        return new Diagnostics.Names(
                js.variables().filter(v -> js.status(v).isActive()).map(VarSnippet::name).collect(Collectors.toSet()),
                js.methods().filter(m -> js.status(m).isActive()).map(m -> m.name()).collect(Collectors.toSet()),
                js.types().filter(t -> js.status(t).isActive()).map(t -> t.name()).collect(Collectors.toSet()));
    }

    private static Set<String> stringVariables(JShell js) {
        return js.variables().filter(v -> js.status(v).isActive() && v.typeName().equals("String"))
                .map(VarSnippet::name).collect(Collectors.toSet());
    }

    /** 防止無窮輸出把記憶體吃光。 */
    private static final class CappedOutput extends OutputStream {
        private final int limit;
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private boolean truncated;

        CappedOutput(int limit) {
            this.limit = limit;
        }

        @Override
        public synchronized void write(int b) {
            if (buffer.size() < limit) {
                buffer.write(b);
            } else {
                truncated = true;
            }
        }

        @Override
        public synchronized void write(byte[] b, int off, int len) {
            int room = limit - buffer.size();
            if (len > room) {
                truncated = true;
            }
            buffer.write(b, off, Math.max(0, Math.min(len, room)));
        }

        synchronized void reset() {
            buffer.reset();
            truncated = false;
        }

        synchronized String text() {
            return buffer.toString(UTF_8);
        }

        synchronized boolean truncated() {
            return truncated;
        }
    }
}
