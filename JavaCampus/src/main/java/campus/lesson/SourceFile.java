package campus.lesson;

/** 編輯器裡的一個檔案。{@code name} 含副檔名，例如 {@code Main.java}；片段題只有一個檔案。 */
public record SourceFile(String name, String text) {

    /** 去掉副檔名的檔名；公開類別要跟它同名。 */
    public String baseName() {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(0, dot);
    }
}
