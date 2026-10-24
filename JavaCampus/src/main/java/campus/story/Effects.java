package campus.story;

import java.util.Set;

/** 劇本 {@code @特效} 可以用的名稱。持續性的會一直留在畫面上，直到寫 {@code @特效 名稱 關}。 */
public final class Effects {

    public static final Set<String> PERSISTENT = Set.of("夕陽", "回憶");
    public static final Set<String> ONCE = Set.of("震動", "閃白", "心跳");

    private Effects() {}

    public static boolean known(String name) {
        return PERSISTENT.contains(name) || ONCE.contains(name);
    }
}
