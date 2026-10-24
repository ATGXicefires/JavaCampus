package campus.engine;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 把 javac 診斷與執行期例外翻成給零基礎玩家看的繁中說明。 */
final class Diagnostics {

    record Names(Set<String> variables, Set<String> methods, Set<String> types) {}

    // 依常用程度排序：編輯距離同分時，越前面的越優先被建議
    private static final List<String> COMMON_TYPES = List.of(
            "String", "System", "Math", "Integer", "Double", "Boolean", "Character", "Long", "Object",
            "StringBuilder", "Arrays", "List", "ArrayList", "Map", "HashMap", "Set", "HashSet", "Scanner",
            "Random", "IO");
    // JShell API 沒有預設的 import，真正的 Java 也只自動帶 java.lang，這些類別都要自己 import
    private static final Map<String, String> IMPORTS = new HashMap<>();

    static {
        imports("java.util", "Scanner", "List", "ArrayList", "LinkedList", "Map", "HashMap", "TreeMap",
                "LinkedHashMap", "Set", "HashSet", "TreeSet", "LinkedHashSet", "Arrays", "Collections", "Random",
                "Optional", "Iterator", "Deque", "ArrayDeque", "Queue", "PriorityQueue", "Objects");
        imports("java.util.stream", "Collectors", "Stream", "IntStream");
        imports("java.time", "LocalDate", "LocalTime", "LocalDateTime", "Duration");
        imports("java.nio.file", "Files", "Path", "Paths");
        imports("java.math", "BigDecimal", "BigInteger");
        imports("java.io", "IOException", "BufferedReader", "InputStreamReader", "File");
    }

    private static final List<String> PRINT_METHODS = List.of("println", "print", "printf", "format");
    private static final List<String> STRING_METHODS = List.of(
            "length", "charAt", "substring", "equals", "equalsIgnoreCase", "indexOf", "contains",
            "startsWith", "endsWith", "toUpperCase", "toLowerCase", "trim", "strip", "split", "replace",
            "isEmpty", "isBlank", "repeat", "compareTo");

    private static final Pattern QUOTED = Pattern.compile("'([^']+)'");
    private static final Pattern SYMBOL = Pattern.compile("symbol:\\s+(variable|class|method|interface|enum)\\s+([^\\s(]+)");
    private static final Pattern LOCATION = Pattern.compile("location:\\s+(.+)");
    private static final Pattern PACKAGE = Pattern.compile("package (\\S+) does not exist");
    private static final Pattern CONVERT = Pattern.compile("incompatible types: (.+?) cannot be converted to (.+)");
    private static final Pattern LOSSY = Pattern.compile("possible lossy conversion from (\\S+) to (\\S+)");
    private static final Pattern ILLEGAL_CHAR = Pattern.compile("illegal character: '\\\\u([0-9a-fA-F]{4})'");
    private static final Pattern BINARY = Pattern.compile("binary operator '([^']+)'\\s+first type:\\s+(\\S+)\\s+second type:\\s+(\\S+)");
    private static final Pattern UNARY = Pattern.compile("bad operand type (\\S+) for unary operator '([^']+)'");
    private static final Pattern APPLY = Pattern.compile("(?:method|constructor) (\\S+) in|found for (\\w+)\\(");
    private static final Pattern INCOMPARABLE = Pattern.compile("incomparable types: (\\S+) and (\\S+)");
    private static final Pattern UNINITIALIZED = Pattern.compile("variable (\\S+) might not");
    private static final Pattern NON_STATIC = Pattern.compile("non-static (?:method|variable) (\\S+) cannot");
    private static final Pattern QUALIFIED = Pattern.compile("\\b(?:[a-z]\\w*\\.)+([A-Z]\\w*)");
    private static final Pattern JSHELL_WRAPPER = Pattern.compile("REPL\\.\\$JShell\\$\\d+\\w*\\.");
    private static final Pattern INDEX = Pattern.compile("(?i)index (-?\\d+) out of bounds for length (\\d+)");
    private static final Pattern NUMBER_INPUT = Pattern.compile("For input string: \"(.*)\"");

    private Diagnostics() {}

    private static void imports(String pkg, String... classes) {
        for (String c : classes) {
            IMPORTS.put(c, pkg);
        }
    }

    /** 要先 import 才能用的常見類別在哪個套件；不在清單裡就是 null。 */
    static String packageOf(String type) {
        return IMPORTS.get(type);
    }

    static String compileError(String code, String english, Names names) {
        String msg = simplifyTypes(english);
        return switch (code) {
            case "compiler.err.expected" -> expected(quoted(msg));
            case "compiler.err.expected2", "compiler.err.expected3", "compiler.err.expected4" ->
                    "這裡少了 " + String.join(" 或 ", allQuoted(msg).stream().map(t -> "`" + t + "`").toList()) + "。";
            case "compiler.err.cant.resolve", "compiler.err.cant.resolve.location",
                 "compiler.err.cant.resolve.args", "compiler.err.cant.resolve.location.args" -> cantResolve(msg, names);
            case "compiler.err.doesnt.exist" -> unknownPackage(msg, names);
            case "compiler.err.prob.found.req" -> incompatible(msg);
            case "compiler.err.unclosed.str.lit" -> "字串少了結尾的雙引號 `\"`。字串要用一對 `\"…\"` 包起來，而且不能跨行。";
            case "compiler.err.unclosed.char.lit" -> "單引號 `'` 只能包住一個字元（char），例如 `'A'`。一段文字要改用雙引號 `\"…\"`。";
            case "compiler.err.empty.char.lit" -> "單引號中間不能是空的，要放一個字元，例如 `'A'`。";
            case "compiler.err.illegal.char" -> illegalChar(msg);
            case "compiler.err.var.might.not.have.been.initialized" -> uninitialized(msg);
            case "compiler.err.missing.ret.stmt" -> "這個方法宣告了會回傳值，但有些情況一路走到結尾都沒有 `return`。";
            case "compiler.err.not.stmt" -> "這一段不是完整的敘述。單獨的值或運算（例如 `x + 1;`）不能自己成為一行，要把它存進變數或印出來。";
            case "compiler.err.premature.eof" -> "程式還沒寫完就結束了：可能少了 `)`、`}` 或 `;`。";
            case "compiler.err.illegal.start.of.expr", "compiler.err.illegal.start.of.type" ->
                    "這裡的寫法不完整，運算式在這裡斷掉了。檢查前面是不是少寫了值，或多打了符號。";
            case "compiler.err.operator.cant.be.applied", "compiler.err.operator.cant.be.applied.1" -> operator(msg);
            case "compiler.err.cant.apply.symbol", "compiler.err.cant.apply.symbols" -> cantApply(msg);
            case "compiler.err.incomparable.types" -> incomparable(msg);
            case "compiler.err.else.without.if" -> "`else` 前面要先有 `if`。檢查 `if` 的 `}` 和 `else` 之間是不是多了分號。";
            case "compiler.err.void.not.allowed.here" -> "這個方法不會回傳值（void），所以不能把它當成一個值來用。";
            case "compiler.err.non-static.cant.be.ref" -> nonStatic(msg);
            case "compiler.err.invalid.meth.decl.ret.type.req" -> "方法名稱前面要寫回傳型別；不回傳任何值就寫 `void`。";
            default -> "編譯器回報了錯誤：" + firstLine(msg);
        };
    }

    static String missingSemicolon() {
        return "這一行的最後少了分號 `;`。Java 的每個敘述都要用分號結尾。";
    }

    static String timeout(Duration limit) {
        return "程式跑了超過 " + limit.toSeconds() + " 秒還沒結束，已經強制停止。檢查迴圈的條件是不是永遠成立。";
    }

    static String stopped() {
        return "已經手動停止程式。";
    }

    static String exited() {
        return "程式呼叫了 `System.exit()`，執行環境已經重新啟動。";
    }

    static String outsideClass() {
        return "這裡只能寫 `import` 或 `class`。變數和方法要寫在 class 的大括號裡面，敘述要寫在方法裡面。";
    }

    static String importAfterType() {
        return "`import` 要寫在檔案最上面，放在所有 class 之前。";
    }

    static String publicClassName(String name, String file) {
        return "`public class " + name + "` 要放在叫 `" + name + ".java` 的檔案裡。這個檔案是 `" + file
                + ".java`，所以公開的類別要叫 `" + file + "`。";
    }

    static String duplicateType(String name) {
        return "已經有一個叫 `" + name + "` 的類別了，同一個名字不能宣告兩次。";
    }

    static String noMainClass(String main) {
        return "找不到 `" + main + "` 類別。程式要從 `" + main + "` 類別的 `main` 方法開始執行。";
    }

    static String noMainMethod(String main) {
        return "`" + main + "` 裡找不到能執行的 `main` 方法。程式的起點要寫成 `public static void main(String[] args)`。";
    }

    static String alreadyDefined(String name, boolean givenByCase) {
        if (givenByCase) {
            return "`" + name + "` 是題目提供的資料，已經宣告好了。直接使用它就好，前面不用再寫型別。";
        }
        return "變數 `" + name + "` 已經宣告過了，不能再宣告一次。要改它的值，直接寫 `" + name + " = …;`。";
    }

    static String runtimeError(String exceptionClass, String message) {
        String simple = exceptionClass.substring(exceptionClass.lastIndexOf('.') + 1);
        String msg = message == null ? "" : cleanWrapperNames(message);
        return switch (simple) {
            case "ArithmeticException" -> msg.contains("/ by zero") ? "除以零了！整數不能除以 0。" : "算術錯誤：" + msg;
            case "NullPointerException" -> "某個值是 null（什麼都沒有），所以不能對它呼叫方法或取用內容。";
            case "ArrayIndexOutOfBoundsException" -> outOfBounds(msg, "陣列");
            case "StringIndexOutOfBoundsException" -> outOfBounds(msg, "字串");
            case "IndexOutOfBoundsException" -> outOfBounds(msg, "清單");
            case "NumberFormatException" -> numberFormat(msg);
            case "ClassCastException" -> "型別轉換失敗：" + simplifyTypes(msg);
            case "StackOverflowError" -> "方法不斷呼叫自己停不下來（遞迴沒有終點），呼叫堆疊滿了。";
            case "OutOfMemoryError" -> "記憶體用完了，可能有集合或字串一直無限制地變大。";
            default -> "程式執行時拋出了例外 `" + simple + "`" + (msg.isEmpty() ? "。" : "：" + msg);
        };
    }

    static String cleanWrapperNames(String s) {
        return JSHELL_WRAPPER.matcher(s).replaceAll("");
    }

    private static String expected(String token) {
        if (token == null) {
            return "這裡少了某個符號。";
        }
        return switch (token) {
            case ";" -> missingSemicolon();
            case ")" -> "這裡少了右括號 `)`。每個 `(` 都要有對應的 `)`。";
            case "(" -> "這裡少了左括號 `(`。";
            case "}" -> "這裡少了右大括號 `}`。每個 `{` 都要有對應的 `}`。";
            case "{" -> "這裡少了左大括號 `{`。";
            case "<identifier>" -> "這裡應該要有一個名字（變數、方法或型別的名稱）。";
            default -> "這裡少了 `" + token + "`。";
        };
    }

    private static String cantResolve(String msg, Names names) {
        Matcher m = SYMBOL.matcher(msg);
        if (!m.find()) {
            return "找不到某個名字：" + firstLine(msg);
        }
        String kind = m.group(1);
        String name = m.group(2);
        // 型別名稱拿來呼叫靜態方法時（例如 Arrays.sort），javac 會把它當成找不到的變數
        if (!kind.equals("method") && IMPORTS.containsKey(name) && !names.types().contains(name)) {
            return "要用 `" + name + "`，得先在檔案最上面寫 `import " + IMPORTS.get(name) + "." + name + ";`。";
        }
        Matcher loc = LOCATION.matcher(msg);
        String location = loc.find() ? loc.group(1) : "";
        Collection<String> candidates;
        String head;
        switch (kind) {
            case "variable" -> {
                candidates = union(names.variables(), COMMON_TYPES);
                head = "找不到變數 `" + name + "`：它還沒有宣告，或是名字拼錯了。";
            }
            case "method" -> {
                if (location.contains("PrintStream")) {
                    candidates = PRINT_METHODS;
                } else if (location.contains("String")) {
                    candidates = STRING_METHODS;
                } else {
                    candidates = names.methods();
                }
                head = "找不到方法 `" + name + "`。";
            }
            default -> {
                candidates = union(names.types(), COMMON_TYPES);
                head = "找不到型別 `" + name + "`。";
            }
        }
        return head + suggestion(name, candidates);
    }

    private static String unknownPackage(String msg, Names names) {
        Matcher m = PACKAGE.matcher(msg);
        if (!m.find()) {
            return "找不到某個名字：" + firstLine(msg);
        }
        String name = m.group(1);
        int dot = name.indexOf('.');
        String first = dot < 0 ? name : name.substring(0, dot);
        return "找不到 `" + first + "`。" + suggestion(first, union(names.variables(), COMMON_TYPES));
    }

    private static String suggestion(String name, Collection<String> candidates) {
        String near = Edits.nearest(name, candidates);
        if (near == null) {
            return "";
        }
        String s = "你是不是要寫 `" + near + "`？";
        if (near.equalsIgnoreCase(name)) {
            s += "Java 會區分大小寫，`" + name + "` 和 `" + near + "` 是不同的名字。";
        }
        return s;
    }

    private static String incompatible(String msg) {
        Matcher lossy = LOSSY.matcher(msg);
        if (lossy.find()) {
            return "把 " + lossy.group(1) + " 放進 " + lossy.group(2) + " 可能會遺失資料（例如小數點後面會不見），Java 不會自動幫你轉。";
        }
        if (msg.contains("unexpected return value")) {
            return "這個方法是 void，不會回傳值，所以 `return` 後面不能接東西。";
        }
        if (msg.contains("missing return value")) {
            return "這個方法要回傳一個值，`return` 後面要寫要回傳的東西。";
        }
        Matcher m = CONVERT.matcher(msg);
        if (!m.find()) {
            return "型別不合：" + firstLine(msg);
        }
        String from = m.group(1).trim();
        String to = firstLine(m.group(2)).trim();
        String s = "型別不合：" + from + " 不能直接放進 " + to + "。";
        if (to.equals("boolean")) {
            s += "這裡需要 true 或 false 的條件，例如 `x > 1`。";
        } else if (from.equals("String") && isNumeric(to)) {
            s += "文字（String）不能直接當成數字用，就算內容看起來像數字也一樣。";
        } else if (to.equals("String") && isNumeric(from)) {
            s += "要把數字變成文字，可以寫 `\"\" + 數字`。";
        }
        return s;
    }

    private static boolean isNumeric(String type) {
        return Set.of("int", "long", "double", "float", "short", "byte").contains(type);
    }

    private static String illegalChar(String msg) {
        Matcher m = ILLEGAL_CHAR.matcher(msg);
        if (!m.find()) {
            return "這裡有 Java 看不懂的字元。";
        }
        char ch = (char) Integer.parseInt(m.group(1), 16);
        if (ch == '“' || ch == '”' || ch == '「' || ch == '」') {
            return "這裡用了中文引號 `" + ch + "`。Java 的字串要用半形雙引號 `\"`。";
        }
        if (ch == '‘' || ch == '’') {
            return "這裡用了中文單引號 `" + ch + "`。Java 要用半形的 `'`。";
        }
        if (ch == '　') {
            return "這裡有一個全形空白，請刪掉，或換成一般的半形空白。";
        }
        if (ch == '、') {
            return "頓號 `、` 不是 Java 的符號，要分隔請用逗號 `,`。";
        }
        if (ch == '。') {
            return "句號 `。` 不是 Java 的符號，敘述的結尾要用分號 `;`。";
        }
        if (ch >= '！' && ch <= '～') {
            char half = (char) (ch - 0xFEE0);
            return "這裡打成了全形的 `" + ch + "`，請換成半形的 `" + half + "`（先切換到英文輸入）。";
        }
        return "這裡有 Java 看不懂的字元 `" + ch + "`。";
    }

    private static String nonStatic(String msg) {
        Matcher m = NON_STATIC.matcher(msg);
        String name = m.find() ? "`" + m.group(1) + "`" : "它";
        return "static 方法裡不能直接使用非 static 的 " + name + "。在它的宣告前面加上 `static`，或先建立物件再透過物件使用。";
    }

    private static String uninitialized(String msg) {
        Matcher m = UNINITIALIZED.matcher(msg);
        String name = m.find() ? m.group(1) : "這個變數";
        return "變數 `" + name + "` 還沒有給值就被使用了。先寫 `" + name + " = …;` 給它一個值。";
    }

    private static String operator(String msg) {
        Matcher b = BINARY.matcher(msg);
        if (b.find()) {
            String op = b.group(1);
            String t1 = b.group(2);
            String t2 = b.group(3);
            String s = "`" + op + "` 不能用在 " + t1 + " 和 " + t2 + " 之間。";
            if ((t1.equals("String") || t2.equals("String")) && Set.of("-", "*", "/", "%").contains(op)) {
                s += "文字（String）只能用 `+` 和別的值串接，不能做其他算術。";
            } else if (op.equals("&&") || op.equals("||")) {
                s += "`&&`、`||` 的兩邊都要是 true 或 false 的條件。";
            }
            return s;
        }
        Matcher u = UNARY.matcher(msg);
        if (u.find()) {
            return "`" + u.group(2) + "` 不能用在 " + u.group(1) + " 上。";
        }
        return "運算子用錯了地方：" + firstLine(msg);
    }

    private static String cantApply(String msg) {
        Matcher m = APPLY.matcher(msg);
        String name = null;
        if (m.find()) {
            name = m.group(1) != null ? m.group(1) : m.group(2);
        }
        return "呼叫 " + (name != null ? "`" + name + "` " : "") + "時給的參數不對：數量或型別和它要的不一樣。";
    }

    private static String incomparable(String msg) {
        Matcher m = INCOMPARABLE.matcher(msg);
        if (!m.find()) {
            return "這兩個值的型別不同，不能互相比較。";
        }
        return m.group(1) + " 和 " + m.group(2) + " 是不同的型別，不能用 `==` 互相比較。";
    }

    private static String outOfBounds(String msg, String what) {
        Matcher m = INDEX.matcher(msg);
        if (!m.find()) {
            return "超出" + what + "的範圍：" + msg;
        }
        int index = Integer.parseInt(m.group(1));
        int length = Integer.parseInt(m.group(2));
        if (length == 0) {
            return "索引 " + index + " 超出範圍：這個" + what + "是空的。";
        }
        return "索引 " + index + " 超出範圍：這個" + what + "的長度是 " + length + "，能用的索引是 0 到 " + (length - 1) + "。";
    }

    private static String numberFormat(String msg) {
        Matcher m = NUMBER_INPUT.matcher(msg);
        if (!m.find()) {
            return "這段文字不是數字，沒辦法轉成數字：" + msg;
        }
        return "「" + m.group(1) + "」不是一個數字，沒辦法轉成數字。";
    }

    private static String simplifyTypes(String s) {
        return QUALIFIED.matcher(cleanWrapperNames(s)).replaceAll("$1");
    }

    private static String quoted(String msg) {
        Matcher m = QUOTED.matcher(msg);
        return m.find() ? m.group(1) : (msg.contains("<identifier>") ? "<identifier>" : null);
    }

    private static List<String> allQuoted(String msg) {
        List<String> out = new ArrayList<>();
        Matcher m = QUOTED.matcher(msg);
        while (m.find()) {
            out.add(m.group(1));
        }
        return out;
    }

    private static String firstLine(String s) {
        int nl = s.indexOf('\n');
        return nl < 0 ? s : s.substring(0, nl);
    }

    private static Set<String> union(Collection<String> a, Collection<String> b) {
        Set<String> s = new LinkedHashSet<>(a);
        s.addAll(b);
        return s;
    }
}
