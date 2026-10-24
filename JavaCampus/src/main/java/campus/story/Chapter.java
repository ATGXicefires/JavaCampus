package campus.story;

/**
 * 一章的三個劇本檔。
 *
 * @param progress 這一章結束時的專題進度百分比，章末畫面的進度條用
 */
public record Chapter(String id, int number, String title, int progress,
                      ScriptFile common, ScriptFile senpai, ScriptFile kouhai) {

    public ScriptFile pov(Route route) {
        return route == Route.SENPAI ? senpai : kouhai;
    }

    public ScriptFile file(String name) {
        if (common.name().equals(name)) {
            return common;
        }
        if (senpai.name().equals(name)) {
            return senpai;
        }
        if (kouhai.name().equals(name)) {
            return kouhai;
        }
        throw new IllegalArgumentException(id + " 沒有劇本檔 " + name);
    }
}
