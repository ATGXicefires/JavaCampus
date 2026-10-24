package campus.story;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 舞台目前的樣子：背景、誰站在哪裡、正在放的音樂。跟著存檔走，讀檔或從程式題回來時畫面才能原樣重建。
 * 欄位保持可變的一般集合，因為 Gson 要直接讀寫。
 */
public final class Staging {

    public static final class Actor {
        private String position;
        private String expression;

        /** 給 Gson 讀檔用。 */
        private Actor() {
        }

        public Actor(String position, String expression) {
            this.position = position;
            this.expression = expression;
        }

        public String position() {
            return position;
        }

        public String expression() {
            return expression;
        }

        void setExpression(String e) {
            expression = e;
        }

        void setPosition(String p) {
            position = p;
        }
    }

    private String background;
    private String variant;
    private Map<String, Actor> actors = new LinkedHashMap<>();
    private String music;
    private List<String> ambient = new ArrayList<>();
    private Set<String> filters = new HashSet<>();
    private String phone;
    private String call;
    private String selfExpression;

    public String background() {
        return background;
    }

    public String variant() {
        return variant;
    }

    /** 依登場順序。 */
    public Map<String, Actor> actors() {
        if (actors == null) {
            actors = new LinkedHashMap<>();
        }
        return actors;
    }

    public String music() {
        return music;
    }

    public List<String> ambient() {
        if (ambient == null) {
            ambient = new ArrayList<>();
        }
        return ambient;
    }

    /** 持續性的畫面濾鏡，例如「夕陽」「回憶」。 */
    public Set<String> filters() {
        if (filters == null) {
            filters = new HashSet<>();
        }
        return filters;
    }

    /** 正在用手機聊天的對象；沒有就是 null。 */
    public String phone() {
        return phone;
    }

    public String call() {
        return call;
    }

    /** 視角角色現在的表情，給對話框的頭像用；舊存檔沒有這個欄位，當成一般。 */
    public String selfExpression() {
        return selfExpression != null ? selfExpression : "一般";
    }

    /** 視角角色不上舞台，劇本寫給他的登場和表情只記下表情，退場不影響。 */
    void applySelf(Op.Cmd c) {
        switch (c.name()) {
            case "登場" -> selfExpression = c.arg(2) != null ? c.arg(2) : "一般";
            case "表情" -> selfExpression = c.arg(1);
            default -> {
            }
        }
    }

    /** 把會留在畫面上的指令記下來；一次性的指令（音效、震動）不影響舞台。 */
    void apply(Op.Cmd c) {
        switch (c.name()) {
            case "背景" -> {
                background = c.arg(0);
                variant = c.arg(1);
            }
            case "登場" -> {
                String expression = c.arg(2) != null ? c.arg(2) : "一般";
                actors().remove(c.arg(0));
                actors().put(c.arg(0), new Actor(c.arg(1), expression));
            }
            case "表情" -> {
                Actor a = actors().get(c.arg(0));
                if (a != null) {
                    a.setExpression(c.arg(1));
                }
            }
            case "退場" -> {
                if (c.arg(0).equals("全部")) {
                    actors().clear();
                } else {
                    actors().remove(c.arg(0));
                }
            }
            case "音樂" -> music = c.arg(0).equals("停") ? null : c.arg(0);
            case "環境" -> {
                ambient().clear();
                if (!c.arg(0).equals("停")) {
                    ambient().addAll(c.args());
                }
            }
            case "特效" -> {
                String name = c.arg(0);
                if (Effects.PERSISTENT.contains(name)) {
                    if ("關".equals(c.arg(1))) {
                        filters().remove(name);
                    } else {
                        filters().add(name);
                    }
                }
            }
            case "手機" -> phone = c.arg(0).equals("收起") ? null : c.arg(0);
            case "電話" -> call = c.arg(0).equals("掛斷") ? null : c.arg(0);
            default -> {
            }
        }
    }

    /** 視角角色不上舞台，劇本裡寫給他的登場、表情、退場都不交給畫面，只由 {@link #applySelf} 記下表情。 */
    static boolean concernsSelf(Op.Cmd c, Route route) {
        return switch (c.name()) {
            case "登場", "表情", "退場" -> Cast.find(c.arg(0)).map(p -> p.id().equals(route.self)).orElse(false);
            default -> false;
        };
    }

    /** 劇本用名字指定角色，名冊裡有的換成代號，同一人的不同寫法才會對到同一個舞台位置。 */
    static Op.Cmd normalize(Op.Cmd c) {
        return switch (c.name()) {
            case "登場", "表情", "退場" -> {
                if (c.arg(0).equals("全部")) {
                    yield c;
                }
                List<String> args = new ArrayList<>(c.args());
                args.set(0, Cast.find(c.arg(0)).map(Cast.Person::id).orElse(c.arg(0)));
                yield new Op.Cmd(c.line(), c.name(), List.copyOf(args));
            }
            default -> c;
        };
    }

    /** 換章時舞台清空。 */
    void clear() {
        background = null;
        variant = null;
        actors().clear();
        music = null;
        ambient().clear();
        filters().clear();
        phone = null;
        call = null;
        selfExpression = null;
    }
}
