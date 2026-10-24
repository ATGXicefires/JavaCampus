package campus.lesson;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 從 {@code lessons/} 讀入所有程式題、筆記條目和搭檔反應。每題一個資料夾，內含 {@code lesson.json}
 * 與每個步驟的程式碼檔：參考解答 {@code solution}、骨架 {@code skeleton}，以及選填的起始碼 {@code starter}。
 * <ul>
 *   <li>只有一個檔案時寫成 {@code <stepId>.<種類>.jsh}，例如 {@code say.starter.jsh}。片段題的分頁叫
 *       「片段.jsh」，完整程式則當成 {@code Main.java}。</li>
 *   <li>完整程式有好幾個檔案時，每個檔案寫成 {@code <stepId>.<種類>.<檔名>.java}，例如 {@code roster.starter.Main.java}
 *       和 {@code roster.starter.Student.java}。分頁的順序是 {@code Main.java} 在最前面，其他照檔名排。</li>
 * </ul>
 * 同一個步驟的同一個種類只能用其中一種寫法。
 */
public record LessonLibrary(Map<String, Lesson> lessons, Handbook handbook, Reactions reactions) {

    private static final Gson GSON = new Gson();

    public static LessonLibrary load(Path root) throws IOException {
        Map<String, Lesson> lessons = new LinkedHashMap<>();
        try (Stream<Path> dirs = Files.list(root)) {
            for (Path dir : dirs.filter(d -> Files.exists(d.resolve("lesson.json"))).sorted().toList()) {
                Lesson l = loadLesson(dir);
                lessons.put(l.id(), l);
            }
        }
        Handbook handbook = GSON.fromJson(Files.readString(root.resolve("handbook.json"), UTF_8), Handbook.class);
        Map<String, Map<String, List<String>>> byPartner = GSON.fromJson(
                Files.readString(root.resolve("reactions.json"), UTF_8),
                new TypeToken<Map<String, Map<String, List<String>>>>() {}.getType());
        LessonLibrary lib = new LessonLibrary(Map.copyOf(lessons), handbook, new Reactions(Map.copyOf(byPartner)));
        lessons.values().forEach(lib::validateLinks);
        return lib;
    }

    public Lesson lesson(String id) {
        Lesson l = lessons.get(id);
        if (l == null) {
            throw new IllegalArgumentException("沒有這道程式題：" + id);
        }
        return l;
    }

    private static Lesson loadLesson(Path dir) throws IOException {
        Lesson raw = GSON.fromJson(Files.readString(dir.resolve("lesson.json"), UTF_8), Lesson.class);
        if (!dir.getFileName().toString().equals(raw.id())) {
            throw new IllegalStateException(dir + "：資料夾名稱要和 id 相同");
        }
        List<String> names;
        try (Stream<Path> files = Files.list(dir)) {
            names = files.map(p -> p.getFileName().toString()).toList();
        }
        List<Step> steps = new ArrayList<>();
        for (Step s : raw.steps()) {
            steps.add(s.withCode(readFiles(dir, names, s, "starter"), readFiles(dir, names, s, "skeleton"),
                    readFiles(dir, names, s, "solution")));
        }
        Lesson l = raw.withSteps(steps);
        validate(l);
        return l;
    }

    /** 讀出一個步驟某個種類的所有檔案；沒有就是空的。 */
    private static List<SourceFile> readFiles(Path dir, List<String> names, Step s, String kind) throws IOException {
        String prefix = s.id() + "." + kind + ".";
        List<String> java = names.stream().filter(n -> n.startsWith(prefix) && n.endsWith(".java")).sorted().toList();
        String where = dir.getFileName() + "/" + s.id() + "：" + kind;
        if (names.contains(prefix + "jsh")) {
            if (!java.isEmpty()) {
                throw new IllegalStateException(where + " 同時有 .jsh 和 .java，只能用其中一種");
            }
            return List.of(new SourceFile(s.form().fileName, Files.readString(dir.resolve(prefix + "jsh"), UTF_8)));
        }
        if (!java.isEmpty() && s.form() != Step.Form.PROGRAM) {
            throw new IllegalStateException(where + " 是片段題，只能用一個 .jsh 檔");
        }
        List<SourceFile> out = new ArrayList<>();
        for (String n : java) {
            out.add(new SourceFile(n.substring(prefix.length()), Files.readString(dir.resolve(n), UTF_8)));
        }
        out.sort(Comparator.comparing((SourceFile f) -> !f.name().equals(Step.Form.PROGRAM.fileName)));
        return out;
    }

    private static void validate(Lesson l) {
        if (!"學長".equals(l.route()) && !"學妹".equals(l.route())) {
            throw new IllegalStateException(l.id() + "：route 要是「學長」或「學妹」");
        }
        if (l.steps().isEmpty()) {
            throw new IllegalStateException(l.id() + "：沒有步驟");
        }
        Set<String> ids = new HashSet<>();
        for (Step s : l.steps()) {
            if (!ids.add(s.id())) {
                throw new IllegalStateException(l.id() + "：步驟 id 重複 " + s.id());
            }
            if (s.check() == null) {
                throw new IllegalStateException(l.id() + "/" + s.id() + "：缺少 check");
            }
            if (s.goal().isBlank()) {
                throw new IllegalStateException(l.id() + "/" + s.id() + "：缺少 goal");
            }
            if (s.skeleton().isEmpty() || s.solution().isEmpty()) {
                throw new IllegalStateException(l.id() + "/" + s.id() + "：缺少 skeleton 或 solution 檔");
            }
            if (s.check().type() == Check.Mode.VALUES && s.check().exprs().isEmpty()) {
                throw new IllegalStateException(l.id() + "/" + s.id() + "：values 檢查沒有指定 exprs");
            }
            if (s.form() == Step.Form.PROGRAM && !s.check().variants().isEmpty()) {
                throw new IllegalStateException(l.id() + "/" + s.id() + "：完整程式不會注入題目變數，"
                        + "variants 沒有作用；改用 exprs 呼叫方法當隱藏測資");
            }
        }
    }

    private void validateLinks(Lesson l) {
        for (String note : l.notes()) {
            if (handbook.entry(note).isEmpty()) {
                throw new IllegalStateException(l.id() + "：筆記條目 " + note + " 不在 handbook.json 裡");
            }
        }
        if (!reactions.has(l.partner())) {
            throw new IllegalStateException(l.id() + "：reactions.json 沒有搭檔「" + l.partner() + "」");
        }
    }
}
