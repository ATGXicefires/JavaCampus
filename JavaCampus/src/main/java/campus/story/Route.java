package campus.story;

import java.util.Arrays;
import java.util.Optional;

/** 玩家扮演的視角。{@code self} 是視角角色在名冊裡的 id，這個角色不會出現在畫面上。 */
public enum Route {
    SENPAI("學長", "二弘", "senpai"),
    KOUHAI("學妹", "亭瑄", "kouhai");

    public final String label;
    public final String self;
    /** 劇本檔名後綴，例如 {@code ch01-senpai.md}。 */
    public final String fileSuffix;

    Route(String label, String self, String fileSuffix) {
        this.label = label;
        this.self = self;
        this.fileSuffix = fileSuffix;
    }

    public static Optional<Route> byLabel(String label) {
        return Arrays.stream(values()).filter(r -> r.label.equals(label)).findFirst();
    }
}
