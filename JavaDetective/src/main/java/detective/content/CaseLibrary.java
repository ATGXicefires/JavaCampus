package detective.content;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 從 {@code cases/} 讀入所有章節。目錄結構：{@code manifest.json} 列出章節與案件順序，
 * 每個案件一個資料夾，內含 {@code case.json} 與每個步驟的 {@code <stepId>.solution.jsh}、
 * {@code <stepId>.skeleton.jsh}，以及選填的 {@code <stepId>.starter.jsh}。
 */
public final class CaseLibrary {

    private record Manifest(List<ChapterRef> chapters) {}

    private record ChapterRef(String id, String title, List<String> cases) {}

    private static final Gson GSON = new Gson();

    private CaseLibrary() {}

    public static List<Chapter> load(Path root) throws IOException {
        Manifest manifest = GSON.fromJson(Files.readString(root.resolve("manifest.json"), UTF_8), Manifest.class);
        List<Chapter> chapters = new ArrayList<>();
        for (ChapterRef ref : manifest.chapters()) {
            List<CaseDef> cases = new ArrayList<>();
            for (String caseId : ref.cases()) {
                cases.add(loadCase(root.resolve(caseId)));
            }
            chapters.add(new Chapter(ref.id(), ref.title(), List.copyOf(cases)));
        }
        return List.copyOf(chapters);
    }

    private static CaseDef loadCase(Path dir) throws IOException {
        CaseDef raw = GSON.fromJson(Files.readString(dir.resolve("case.json"), UTF_8), CaseDef.class);
        List<Step> steps = new ArrayList<>();
        for (Step s : raw.steps()) {
            String starter = readOptional(dir.resolve(s.id() + ".starter.jsh"));
            String skeleton = Files.readString(dir.resolve(s.id() + ".skeleton.jsh"), UTF_8);
            String solution = Files.readString(dir.resolve(s.id() + ".solution.jsh"), UTF_8);
            steps.add(s.withCode(starter, skeleton, solution));
        }
        CaseDef c = raw.withSteps(steps);
        validate(c);
        return c;
    }

    private static String readOptional(Path p) throws IOException {
        return Files.exists(p) ? Files.readString(p, UTF_8) : "";
    }

    private static void validate(CaseDef c) {
        Set<String> ids = new HashSet<>();
        for (Step s : c.steps()) {
            if (!ids.add(s.id())) {
                throw new IllegalStateException(c.id() + "：步驟 id 重複 " + s.id());
            }
            if (s.check() == null) {
                throw new IllegalStateException(c.id() + "/" + s.id() + "：缺少 check");
            }
            if (s.check().type() == Check.Mode.VALUES && s.check().exprs().isEmpty()) {
                throw new IllegalStateException(c.id() + "/" + s.id() + "：values 檢查沒有指定 exprs");
            }
            if (s.form() == Step.Form.PROGRAM && !s.check().variants().isEmpty()) {
                throw new IllegalStateException(c.id() + "/" + s.id() + "：完整程式不會注入案件變數，"
                        + "variants 沒有作用；改用 exprs 呼叫方法當隱藏測資");
            }
        }
        for (Evidence e : c.evidence()) {
            if (e.unlockOnStep() != null && !ids.contains(e.unlockOnStep())) {
                throw new IllegalStateException(c.id() + "：證物 " + e.id() + " 指向不存在的步驟 " + e.unlockOnStep());
            }
        }
    }
}
