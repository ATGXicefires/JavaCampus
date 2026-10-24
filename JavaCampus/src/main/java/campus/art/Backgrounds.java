package campus.art;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 程式繪製的背景。每次都依畫布實際大小用比例座標重畫，所以 1K 到 2K、16:9 到 16:10 都不會糊。
 * 名稱和變體就是劇本 {@code @背景 名稱 變體} 寫的字。
 */
public final class Backgrounds {

    /** 名稱對應可以用的變體；第一個是沒寫變體時的預設。 */
    public static final Map<String, List<String>> NAMES = Map.of(
            "電腦教室", List.of("白天", "傍晚", "雨夜"),
            "校門口", List.of("雨"),
            "化工科實驗室外", List.of("白天"),
            "亭瑄的房間", List.of("夜"));

    private Backgrounds() {}

    public static boolean known(String name, String variant) {
        List<String> variants = NAMES.get(name);
        return variants != null && (variant == null || variants.contains(variant));
    }

    /** 雨只該落在窗外的背景，回傳那扇窗在畫面上的比例位置；整片都可以下雨就是 null。 */
    public static Rectangle2D window(String name) {
        return switch (name) {
            case "電腦教室" -> new Rectangle2D(0.02, 0.08, 0.26, 0.5);
            case "亭瑄的房間" -> new Rectangle2D(0.70, 0.12, 0.2, 0.32);
            default -> null;
        };
    }

    public static void paint(GraphicsContext g, double w, double h, String name, String variant) {
        String v = variant != null ? variant : NAMES.getOrDefault(name, List.of("")).getFirst();
        g.save();
        g.clearRect(0, 0, w, h);
        switch (name) {
            case "電腦教室" -> classroom(new Pen(g, w, h), v);
            case "校門口" -> gate(new Pen(g, w, h));
            case "化工科實驗室外" -> labCorridor(new Pen(g, w, h));
            case "亭瑄的房間" -> bedroom(new Pen(g, w, h));
            default -> {
                g.setFill(Color.web("#f6efe4"));
                g.fillRect(0, 0, w, h);
            }
        }
        g.restore();
    }

    /** 用 0 到 1 的比例座標畫圖，換算成畫布的像素。 */
    record Pen(GraphicsContext g, double w, double h) {

        double x(double u) {
            return u * w;
        }

        double y(double v) {
            return v * h;
        }

        void rect(double u, double v, double du, double dv, Color c) {
            g.setFill(c);
            g.fillRect(x(u), y(v), du * w, dv * h);
        }

        void vgrad(double u, double v, double du, double dv, String... colors) {
            Stop[] stops = new Stop[colors.length];
            for (int i = 0; i < colors.length; i++) {
                stops[i] = new Stop(colors.length == 1 ? 0 : (double) i / (colors.length - 1), Color.web(colors[i]));
            }
            g.setFill(new LinearGradient(0, y(v), 0, y(v + dv), false, CycleMethod.NO_CYCLE, stops));
            g.fillRect(x(u), y(v), du * w, dv * h);
        }

        void glow(double u, double v, double radius, Color c) {
            g.setFill(new RadialGradient(0, 0, x(u), y(v), radius * Math.max(w, h), false, CycleMethod.NO_CYCLE,
                    new Stop(0, c), new Stop(1, c.deriveColor(0, 1, 1, 0))));
            g.fillRect(0, 0, w, h);
        }

        void poly(Color c, double... uv) {
            double[] xs = new double[uv.length / 2];
            double[] ys = new double[uv.length / 2];
            for (int i = 0; i < xs.length; i++) {
                xs[i] = x(uv[2 * i]);
                ys[i] = y(uv[2 * i + 1]);
            }
            g.setFill(c);
            g.fillPolygon(xs, ys, xs.length);
        }

        void line(Color c, double width, double u0, double v0, double u1, double v1) {
            g.setStroke(c);
            g.setLineWidth(width * h / 900);
            g.strokeLine(x(u0), y(v0), x(u1), y(v1));
        }
    }

    private static void classroom(Pen p, String variant) {
        boolean dusk = variant.equals("傍晚");
        boolean night = variant.equals("雨夜");
        String[] wall = night ? new String[] {"#2b3243", "#232938"} : dusk ? new String[] {"#f3d8bb", "#e4b994"} : new String[] {"#eef0ea", "#dfe3da"};
        p.vgrad(0, 0, 1, 0.7, wall);

        for (int i = 0; i < 3; i++) {
            Color tube = night ? Color.web("#cfe6ff", i == 1 ? 0.9 : 0.25) : Color.web("#ffffff", dusk ? 0.55 : 0.95);
            p.rect(0.36 + i * 0.2, 0.015, 0.12, 0.012, tube);
        }

        double wx = 0.02;
        double wy = 0.08;
        double ww = 0.26;
        double wh = 0.5;
        String[] sky = night ? new String[] {"#16223a", "#0e1626"} : dusk ? new String[] {"#f4a462", "#ee8a77", "#a77aa6"} : new String[] {"#b9dcf2", "#e6f3fb"};
        p.vgrad(wx, wy, ww, wh, sky);
        if (dusk) {
            p.g().save();
            p.g().beginPath();
            p.g().rect(p.x(wx), p.y(wy), ww * p.w(), wh * p.h());
            p.g().clip();
            p.glow(0.2, 0.5, 0.18, Color.web("#ffd27a", 0.85));
            p.g().restore();
        }
        if (!night) {
            Color trees = dusk ? Color.web("#5b4a5e", 0.8) : Color.web("#7fae7a");
            for (int i = 0; i < 5; i++) {
                double cx = wx + 0.03 + i * 0.055;
                p.g().setFill(trees);
                p.g().fillOval(p.x(cx - 0.035), p.y(wy + wh - 0.12), 0.07 * p.w(), 0.16 * p.h());
            }
        }
        Color frame = night ? Color.web("#47506a") : Color.web("#c9cfca");
        p.g().setStroke(frame);
        p.g().setLineWidth(p.h() * 0.012);
        p.g().strokeRect(p.x(wx), p.y(wy), ww * p.w(), wh * p.h());
        p.line(frame, 8, wx + ww / 2, wy, wx + ww / 2, wy + wh);
        p.line(frame, 8, wx, wy + wh * 0.45, wx + ww, wy + wh * 0.45);

        Color board = night ? Color.web("#3c4457") : Color.web("#fbfbf8");
        p.rect(0.4, 0.12, 0.42, 0.28, board);
        p.g().setStroke(night ? Color.web("#59627a") : Color.web("#b9bfbd"));
        p.g().setLineWidth(p.h() * 0.008);
        p.g().strokeRect(p.x(0.4), p.y(0.12), 0.42 * p.w(), 0.28 * p.h());
        Color marker = night ? Color.web("#7f8aa6", 0.6) : Color.web("#5a7bb5", 0.55);
        double[] lens = {0.2, 0.26, 0.14, 0.3, 0.18, 0.1};
        for (int i = 0; i < lens.length; i++) {
            double indent = i == 2 || i == 3 ? 0.03 : 0;
            p.line(marker, 4, 0.43 + indent, 0.16 + i * 0.035, 0.43 + indent + lens[i], 0.16 + i * 0.035);
        }
        p.rect(0.4, 0.40, 0.42, 0.012, night ? Color.web("#4a5268") : Color.web("#c4c9c4"));

        p.g().setFill(night ? Color.web("#3a4256") : Color.web("#f7f7f2"));
        p.g().fillOval(p.x(0.885), p.y(0.07), 0.05 * p.h(), 0.05 * p.h());
        p.g().setStroke(night ? Color.web("#6a7390") : Color.web("#8a8f8c"));
        p.g().setLineWidth(p.h() * 0.004);
        p.g().strokeOval(p.x(0.885), p.y(0.07), 0.05 * p.h(), 0.05 * p.h());

        String[] floor = night ? new String[] {"#252b38", "#1a1f29"} : dusk ? new String[] {"#d9b996", "#c49a77"} : new String[] {"#cfd4cc", "#b8bfb6"};
        p.vgrad(0, 0.62, 1, 0.38, floor);

        desks(p, 0.6, 0.075, 9, 0.6, night, dusk);
        desks(p, 0.74, 0.11, 6, 0.8, night, dusk);
        if (night) {
            p.glow(0.6, 0.75, 0.6, Color.web("#7fd3e6", 0.08));
        } else if (dusk) {
            p.glow(0.15, 0.4, 0.7, Color.web("#ffb36b", 0.18));
        }
    }

    /** 一排電腦：螢幕加椅背，越前排越大。 */
    private static void desks(Pen p, double top, double size, int count, double alpha, boolean night, boolean dusk) {
        double gap = (1.0 - 0.06) / count;
        for (int i = 0; i < count; i++) {
            double cx = 0.03 + gap * (i + 0.5);
            double mw = size;
            double mh = size * 0.62 * p.w() / p.h();
            Color body = night ? Color.web("#151a22") : Color.web("#2f3540");
            Color screen = night ? Color.web("#7fd3e6", 0.9) : dusk ? Color.web("#cfe2ea", 0.85) : Color.web("#a9c2cf");
            p.rect(cx - mw / 2, top, mw, mh, body);
            p.rect(cx - mw / 2 + 0.004, top + 0.006, mw - 0.008, mh - 0.014, screen);
            if (night) {
                p.glow(cx, top + mh / 2, size * 0.9, Color.web("#7fd3e6", 0.12));
            }
            p.rect(cx - 0.006, top + mh, 0.012, mh * 0.25, body);
            p.rect(cx - mw * 0.7, top + mh * 1.25, mw * 1.4, 0.012, night ? Color.web("#3a4152") : Color.web("#d8d2c4"));
            Color chair = night ? Color.web("#10141b", alpha) : Color.web("#4b5361", alpha);
            p.g().setFill(chair);
            double chw = mw * 0.75;
            p.g().fillRoundRect(p.x(cx - chw / 2), p.y(top + mh * 1.35), chw * p.w(), mh * 1.1 * p.h(),
                    chw * p.w() * 0.3, chw * p.w() * 0.3);
        }
    }

    private static void gate(Pen p) {
        p.vgrad(0, 0, 1, 0.65, "#4d5563", "#7b838f");
        p.rect(0.12, 0.24, 0.76, 0.4, Color.web("#6c7480"));
        p.rect(0.12, 0.22, 0.76, 0.03, Color.web("#5b626d"));
        Random r = new Random(3);
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 10; col++) {
                boolean lit = row == 1 && col == 6;
                Color c = lit ? Color.web("#ffd88a") : Color.web("#4a515c", 0.6 + r.nextDouble() * 0.3);
                p.rect(0.15 + col * 0.072, 0.28 + row * 0.085, 0.045, 0.05, c);
            }
        }
        p.glow(0.15 + 6 * 0.072 + 0.022, 0.28 + 0.085 + 0.025, 0.06, Color.web("#ffd88a", 0.35));
        p.vgrad(0, 0.62, 1, 0.38, "#3a3f47", "#23272d");
        p.vgrad(0.582, 0.64, 0.016, 0.36, "#ffd88a", "#ffd88a00");
        for (double u : new double[] {0.04, 0.86}) {
            p.rect(u, 0.3, 0.1, 0.7, Color.web("#2f333a"));
            p.rect(u - 0.01, 0.28, 0.12, 0.03, Color.web("#3b4048"));
        }
        for (int i = 0; i < 6; i++) {
            p.line(Color.web("#565c66"), 3, 0.14, 0.74 + i * 0.02, 0.86, 0.74 + i * 0.02);
        }
        p.g().setGlobalAlpha(0.25);
        p.rect(0, 0, 1, 1, Color.web("#9aa4b2"));
        p.g().setGlobalAlpha(1);
    }

    private static void labCorridor(Pen p) {
        p.vgrad(0, 0, 1, 1, "#e9ece6", "#d9ddd5");
        p.poly(Color.web("#c7ccc4"), 0, 0.75, 1, 0.75, 1, 1, 0, 1);
        p.poly(Color.web("#bfc4bb"), 0.55, 0.75, 1, 0.75, 1, 1, 0.4, 1);
        p.vgrad(0.62, 0.05, 0.38, 0.55, "#bfe0f2", "#e4f2f8");
        Color trees = Color.web("#7fb07a");
        for (int i = 0; i < 4; i++) {
            p.g().setFill(trees);
            p.g().fillOval(p.x(0.64 + i * 0.09), p.y(0.3 + (i % 2) * 0.05), 0.12 * p.w(), 0.3 * p.h());
        }
        p.rect(0.6, 0.0, 0.03, 0.75, Color.web("#d4d8cf"));
        p.rect(0.6, 0.55, 0.4, 0.04, Color.web("#cfd3ca"));
        for (int i = 0; i < 8; i++) {
            p.rect(0.63 + i * 0.05, 0.59, 0.008, 0.16, Color.web("#b6bbb2"));
        }
        p.rect(0.6, 0.73, 0.4, 0.02, Color.web("#b6bbb2"));

        p.rect(0.04, 0.12, 0.5, 0.5, Color.web("#f3f4ef"));
        for (int pane = 0; pane < 2; pane++) {
            double px = 0.07 + pane * 0.24;
            p.vgrad(px, 0.18, 0.2, 0.28, "#d7e7ea", "#c3d8dc");
            String[] liquids = {"#8e7cc3", "#6aa84f", "#e3b341", "#5aa0d8", "#ec7f74"};
            for (int t = 0; t < 5; t++) {
                double tx = px + 0.025 + t * 0.032;
                p.rect(tx, 0.32, 0.014, 0.1, Color.web("#ffffff", 0.75));
                p.rect(tx, 0.37, 0.014, 0.05, Color.web(liquids[(t + pane * 2) % liquids.length]));
            }
            p.rect(px + 0.01, 0.42, 0.18, 0.012, Color.web("#9aa39b"));
            p.poly(Color.web("#ffffff", 0.7), px + 0.13, 0.42, px + 0.18, 0.42, px + 0.165, 0.36, px + 0.145, 0.36);
            p.g().setStroke(Color.web("#a7aea6"));
            p.g().setLineWidth(p.h() * 0.008);
            p.g().strokeRect(p.x(px), p.y(0.18), 0.2 * p.w(), 0.28 * p.h());
        }
        p.rect(0.2, 0.06, 0.16, 0.045, Color.web("#2f5b4a"));
        p.g().setFill(Color.web("#f3f4ef"));
        p.g().setFont(Font.font("Source Han Sans TC", FontWeight.BOLD, p.h() * 0.026));
        p.g().setTextAlign(TextAlignment.CENTER);
        p.g().fillText("化工實驗室", p.x(0.28), p.y(0.093));
        p.glow(0.8, 0.2, 0.6, Color.web("#ffffff", 0.25));
    }

    private static void bedroom(Pen p) {
        p.vgrad(0, 0, 1, 1, "#3d3448", "#2a2433");
        p.vgrad(0.72, 0.14, 0.18, 0.3, "#1a2440", "#26335a");
        Random r = new Random(5);
        for (int i = 0; i < 14; i++) {
            double s = 0.002 + r.nextDouble() * 0.003;
            p.g().setFill(Color.web("#ffffff", 0.5 + r.nextDouble() * 0.5));
            p.g().fillOval(p.x(0.72 + r.nextDouble() * 0.18), p.y(0.14 + r.nextDouble() * 0.28), s * p.w(), s * p.w());
        }
        p.g().setStroke(Color.web("#5a4f66"));
        p.g().setLineWidth(p.h() * 0.012);
        p.g().strokeRect(p.x(0.72), p.y(0.14), 0.18 * p.w(), 0.3 * p.h());
        p.line(Color.web("#5a4f66"), 8, 0.81, 0.14, 0.81, 0.44);

        String[] papers = {"#fff8ec", "#fdf3d8", "#f7ecf2", "#eef5f8"};
        for (int i = 0; i < 6; i++) {
            double px = 0.08 + (i % 3) * 0.13;
            double py = 0.12 + (i / 3) * 0.2;
            p.g().save();
            p.g().translate(p.x(px + 0.05), p.y(py + 0.07));
            p.g().rotate((r.nextDouble() - 0.5) * 8);
            p.g().setFill(Color.web(papers[i % papers.length]));
            p.g().fillRect(-0.05 * p.w(), -0.07 * p.h(), 0.1 * p.w(), 0.14 * p.h());
            p.g().setStroke(Color.web("#ec7f74", 0.8));
            p.g().setLineWidth(p.h() * 0.003);
            p.g().strokeRect(-0.03 * p.w(), -0.04 * p.h(), 0.06 * p.w(), 0.035 * p.h());
            p.g().strokeOval(-0.015 * p.w(), 0.005 * p.h(), 0.03 * p.w(), 0.03 * p.h());
            p.g().restore();
            p.g().setFill(Color.web("#e3b341"));
            p.g().fillOval(p.x(px + 0.047), p.y(py - 0.006), 0.006 * p.w(), 0.006 * p.w());
        }

        p.glow(0.32, 0.55, 0.45, Color.web("#ffcf8a", 0.45));
        p.rect(0, 0.64, 0.62, 0.04, Color.web("#8a6a52"));
        p.rect(0.02, 0.68, 0.58, 0.32, Color.web("#6e5442"));
        p.line(Color.web("#3a3330"), 6, 0.4, 0.64, 0.44, 0.44);
        p.line(Color.web("#3a3330"), 6, 0.44, 0.44, 0.36, 0.38);
        p.poly(Color.web("#f0c27a"), 0.31, 0.42, 0.41, 0.36, 0.43, 0.4, 0.34, 0.47);
        p.poly(Color.web("#ffe2a8", 0.18), 0.32, 0.45, 0.42, 0.39, 0.55, 0.64, 0.12, 0.64);

        p.poly(Color.web("#f2f2f0"), 0.12, 0.64, 0.3, 0.64, 0.29, 0.62, 0.13, 0.62);
        p.poly(Color.web("#e8e9e7"), 0.13, 0.62, 0.29, 0.62, 0.27, 0.48, 0.15, 0.48);
        p.poly(Color.web("#bfe3f2", 0.9), 0.14, 0.6, 0.28, 0.6, 0.265, 0.495, 0.155, 0.495);

        p.rect(0.66, 0.66, 0.34, 0.34, Color.web("#7d6d93"));
        p.rect(0.66, 0.62, 0.34, 0.06, Color.web("#efe6f2"));
        String[] caps = {"#ec7f74", "#e3b341", "#5aa0d8", "#6aa84f", "#8e7cc3"};
        p.rect(0.72, 0.5, 0.24, 0.012, Color.web("#5a4f66"));
        for (int i = 0; i < caps.length; i++) {
            double cx = 0.74 + i * 0.045;
            double d = 0.032;
            p.g().setFill(Color.web("#fbfbf8"));
            p.g().fillOval(p.x(cx), p.y(0.5) - d * p.w(), d * p.w(), d * p.w());
            p.g().setFill(Color.web(caps[i]));
            p.g().fillArc(p.x(cx), p.y(0.5) - d * p.w(), d * p.w(), d * p.w(), 0, 180, javafx.scene.shape.ArcType.CHORD);
        }
    }
}
