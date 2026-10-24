package campus.story;

/** 依 {@code story/outline/endings.md} 判定結局，由上往下符合第一個就是。 */
public enum Endings {
    TRUE("True", "1+1>2"),
    GOOD("Good", "Merge Request Approved"),
    NORMAL("Normal", "Pull Request Pending"),
    BAD("Bad", "Stack Overflow");

    /** 劇本 {@code @如果 結局=...} 用的寫法。 */
    public final String code;
    public final String title;

    Endings(String code, String title) {
        this.code = code;
        this.title = title;
    }

    public static Endings of(int ap, int tp, int keys) {
        if (ap >= 80 && tp >= 80 && keys >= 2) {
            return TRUE;
        }
        if (ap >= 60 && tp >= 60) {
            return GOOD;
        }
        if (ap >= 40 || tp >= 40) {
            return NORMAL;
        }
        return BAD;
    }
}
