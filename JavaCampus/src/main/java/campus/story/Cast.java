package campus.story;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/** 角色名冊。劇本裡同一個人可以有好幾種寫法，名牌照劇本顯示，顏色和嗶聲跟著角色走。 */
public final class Cast {

    /**
     * @param id    名冊裡的代號，也是繪製器、圖檔資料夾、嗶聲用的鍵
     * @param color 代表色，用在名牌、剪影和手機頭像
     */
    public record Person(String id, String fullName, Set<String> aliases, String color) {}

    public static final List<Person> ALL = List.of(
            new Person("二弘", "吳二弘", Set.of("二弘", "吳二弘", "學長", "吳學長"), "#5b7699"),
            new Person("亭瑄", "梁亭瑄", Set.of("亭瑄", "梁亭瑄", "學妹"), "#ec7f74"),
            new Person("阿成", "李有成", Set.of("阿成", "李有成"), "#6aa84f"),
            new Person("小芸", "燕韋芸", Set.of("小芸", "燕韋芸"), "#8e7cc3"),
            new Person("王老師", "王葉聖", Set.of("王老師", "王葉聖"), "#a67c52"),
            new Person("阿嬤", "阿嬤", Set.of("阿嬤"), "#d9774b"));

    public static final String NEUTRAL_COLOR = "#8a7f78";

    private Cast() {}

    public static Optional<Person> find(String name) {
        return ALL.stream().filter(p -> p.aliases().contains(name)).findFirst();
    }

    public static String colorOf(String name) {
        return find(name).map(Person::color).orElse(NEUTRAL_COLOR);
    }
}
