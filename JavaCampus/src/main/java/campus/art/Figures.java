package campus.art;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 程式繪製的角色：純黑剪影、標準 A-pose，邊緣用角色代表色做霓虹描邊（{@link #neon}）。
 * 剪影裡只畫看得見的細節：二弘的眼鏡、小芸的護目鏡、臉紅。表情靠頭頂的符號。
 * 畫在 480×900 的單位框裡（下緣切在大腿），再等比例縮放到畫布。
 */
public final class Figures {

    public static final Set<String> PEOPLE = Set.of("二弘", "亭瑄", "阿成", "小芸", "王老師");
    public static final List<String> EXPRESSIONS = List.of("一般", "笑", "生氣", "害羞", "驚訝", "疑惑", "無言", "冒汗", "心動", "難過");

    public static final double UNIT_W = 480;
    public static final double UNIT_H = 900;

    /** 角色本體在 400 寬的座標裡畫，左右各留 40 給張開的手臂。 */
    private static final double MARGIN = 40;

    private static final Color INK = Color.web("#0c0b10");
    private static final Color BLUSH = Color.web("#ff7a9a");

    /** 霓虹描邊的顏色：代表色調亮，在暗的背景上才發得出光。 */
    private static final Map<String, String> NEON = Map.of(
            "二弘", "#7fb0ff", "亭瑄", "#ff8ea0", "阿成", "#7be88a",
            "小芸", "#b9a2ff", "王老師", "#ffbf7a", "阿嬤", "#ff9d6e");

    /**
     * 頭的位置和大小，表情符號和臉紅依它擺放。
     *
     * @param mirrored 頭的右邊有東西（亭瑄的馬尾），符號改放左邊
     */
    private record Head(double cx, double cy, double rx, double ry, boolean mirrored) {}

    private static final Map<String, Head> HEADS = Map.of(
            "二弘", new Head(200, 202, 82, 88, false),
            "亭瑄", new Head(200, 224, 72, 78, true),
            "阿成", new Head(200, 210, 80, 86, false),
            "小芸", new Head(200, 262, 66, 72, false),
            "王老師", new Head(200, 186, 74, 86, false));

    private static final Head DEFAULT_HEAD = new Head(200, 214, 76, 84, false);

    private Figures() {}

    public static Color neonColor(String person) {
        return Color.web(NEON.getOrDefault(person, "#e8e2d6"));
    }

    /** 剪影的霓虹燈：內側一圈細亮邊，外側一圈光暈。半徑以畫布像素計，依角色顯示的高度調整。 */
    public static Effect neon(String person, double height) {
        Color c = neonColor(person);
        double k = height / UNIT_H;
        InnerShadow rim = new InnerShadow(BlurType.GAUSSIAN, c.deriveColor(0, 0.6, 1.4, 1), 6 * k, 0.75, 0, 0);
        DropShadow glow = new DropShadow(BlurType.GAUSSIAN, c, 26 * k, 0.32, 0, 0);
        glow.setInput(rim);
        return glow;
    }

    public static void paint(GraphicsContext g, double w, double h, String person, String expression) {
        String expr = EXPRESSIONS.contains(expression) ? expression : "一般";
        double s = h / UNIT_H;
        g.save();
        g.clearRect(0, 0, w, h);
        g.translate((w - UNIT_W * s) / 2, 0);
        g.scale(s, s);
        g.translate(MARGIN, 0);
        g.setLineCap(StrokeLineCap.ROUND);
        g.setLineJoin(StrokeLineJoin.ROUND);
        g.setFill(INK);
        switch (person) {
            case "二弘" -> erhong(g, expr);
            case "亭瑄" -> tingxuan(g);
            case "阿成" -> acheng(g);
            case "小芸" -> xiaoyun(g);
            case "王老師" -> teacher(g);
            default -> generic(g);
        }
        if (expr.equals("害羞") || expr.equals("心動")) {
            blush(g, HEADS.getOrDefault(person, DEFAULT_HEAD));
        }
        g.restore();
    }

    private static void oval(GraphicsContext g, double cx, double cy, double rx, double ry) {
        g.fillOval(cx - rx, cy - ry, rx * 2, ry * 2);
    }

    private static void poly(GraphicsContext g, double... xy) {
        double[] xs = new double[xy.length / 2];
        double[] ys = new double[xy.length / 2];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = xy[2 * i];
            ys[i] = xy[2 * i + 1];
        }
        g.fillPolygon(xs, ys, xs.length);
    }

    private static void head(GraphicsContext g, Head h, boolean ears) {
        oval(g, h.cx(), h.cy(), h.rx(), h.ry());
        if (ears) {
            oval(g, h.cx() - h.rx() - 2, h.cy() + 10, 13, 20);
            oval(g, h.cx() + h.rx() + 2, h.cy() + 10, 13, 20);
        }
    }

    /** 圓肩的上半身，從肩膀收到腰。 */
    private static void torso(GraphicsContext g, double shoulderY, double l, double r, double waistY, double wl, double wr) {
        g.beginPath();
        g.moveTo(wl, waistY);
        g.lineTo(l, shoulderY + 40);
        g.quadraticCurveTo(l + 4, shoulderY, l + 50, shoulderY - 6);
        g.lineTo(r - 50, shoulderY - 6);
        g.quadraticCurveTo(r - 4, shoulderY, r, shoulderY + 40);
        g.lineTo(wr, waistY);
        g.closePath();
        g.fill();
    }

    /** A-pose 的兩隻手臂：從肩膀斜斜往外下方張開，和身體之間留一道縫，剪影才看得出是手。 */
    private static void arms(GraphicsContext g, double shoulderX, double shoulderY, double handX, double handY, double width) {
        for (int side = 0; side < 2; side++) {
            boolean left = side == 0;
            poly(g,
                    mirror(left, shoulderX - width * 0.45), shoulderY - 8,
                    mirror(left, shoulderX + width * 0.5), shoulderY - 8,
                    mirror(left, shoulderX + width * 0.65), shoulderY + 84,
                    mirror(left, handX + width * 0.4), handY,
                    mirror(left, handX - width * 0.4), handY);
            oval(g, mirror(left, handX), handY + width * 0.35, width * 0.46, width * 0.6);
        }
    }

    private static double mirror(boolean left, double x) {
        return left ? x : 400 - x;
    }

    /** 兩條腿從上面那塊（褲頭或裙襬）裡面長出來，中間是 V 字分開，接縫處才不會透出光。 */
    private static void legs(GraphicsContext g, double top, double l, double r, double gap) {
        double mid = 200;
        poly(g, l, top - 12, mid - 1, top - 12, mid - gap / 2, top + 40, mid - gap / 2 - 4, UNIT_H + 10, l + 14, UNIT_H + 10);
        poly(g, r, top - 12, mid + 1, top - 12, mid + gap / 2, top + 40, mid + gap / 2 + 4, UNIT_H + 10, r - 14, UNIT_H + 10);
    }

    private static void blush(GraphicsContext g, Head h) {
        g.setStroke(BLUSH);
        g.setLineWidth(4);
        for (double cx : new double[] {h.cx() - h.rx() * 0.5, h.cx() + h.rx() * 0.5}) {
            for (int i = 0; i < 3; i++) {
                double x = cx - 14 + i * 11;
                double y = h.cy() + h.ry() * 0.3;
                g.strokeLine(x, y + 9, x + 8, y - 4);
            }
        }
    }

    /** 吳二弘：厚實、肩膀寬、圓臉；頂上的頭髮往上抓還翹著一撮；黑框方眼鏡；襯衫下擺一角沒紮好。 */
    private static void erhong(GraphicsContext g, String expr) {
        Head h = HEADS.get("二弘");
        arms(g, 66, 352, 4, 712, 50);
        torso(g, 340, 46, 354, 790, 84, 316);
        poly(g, 84, 780, 316, 780, 320, 812, 80, 812);
        legs(g, 812, 82, 318, 16);
        poly(g, 300, 760, 340, 778, 318, 806, 296, 790);
        g.fillRect(166, 260, 68, 90);
        head(g, h, true);

        g.beginPath();
        g.moveTo(122, 176);
        g.bezierCurveTo(112, 112, 160, 66, 220, 68);
        g.bezierCurveTo(272, 70, 300, 108, 290, 170);
        g.lineTo(250, 150);
        g.lineTo(150, 150);
        g.closePath();
        g.fill();
        g.beginPath();
        g.moveTo(224, 76);
        g.quadraticCurveTo(236, 36, 268, 38);
        g.quadraticCurveTo(248, 52, 242, 82);
        g.closePath();
        g.fill();
        g.beginPath();
        g.moveTo(132, 132);
        g.quadraticCurveTo(110, 120, 104, 98);
        g.quadraticCurveTo(124, 108, 146, 112);
        g.closePath();
        g.fill();

        glasses(g, h, expr.equals("無言") || expr.equals("害羞"));
    }

    /** 黑框方眼鏡照一般眼鏡畫：深色鏡片、亮色鏡框、斜斜的反光；要藏住眼神的表情，反光整片蓋住鏡片。 */
    private static void glasses(GraphicsContext g, Head h, boolean glare) {
        double lw = 64;
        double lh = 42;
        double y = h.cy() + 2;
        Color frame = Color.web("#dbe8ff");
        for (double cx : new double[] {h.cx() - 36, h.cx() + 36}) {
            double x = cx - lw / 2;
            double top = y - lh / 2;
            if (glare) {
                g.setFill(new LinearGradient(0, top, 0, top + lh, false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#ffffff")), new Stop(1, Color.web("#cfe0f5"))));
            } else {
                g.setFill(new LinearGradient(0, top, 0, top + lh, false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#2c3d58")), new Stop(1, Color.web("#151d2b"))));
            }
            g.fillRect(x, top, lw, lh);
            g.setStroke(frame);
            g.setLineWidth(4);
            g.strokeRect(x, top, lw, lh);
            g.setLineWidth(8);
            g.strokeLine(x - 1, top, x + lw + 1, top);
            g.setStroke(glare ? Color.web("#9fb8d8") : Color.web("#ffffff", 0.85));
            g.setLineWidth(glare ? 5 : 4);
            g.strokeLine(x + 12, top + lh - 8, x + 28, top + 8);
            g.strokeLine(x + 32, top + lh - 6, x + 42, top + 14);
        }
        g.setStroke(frame);
        g.setLineWidth(4);
        g.strokeLine(h.cx() - 4, y - 12, h.cx() + 4, y - 12);
        g.strokeLine(h.cx() - 36 - lw / 2, y - 16, h.cx() - h.rx() - 2, y - 20);
        g.strokeLine(h.cx() + 36 + lw / 2, y - 16, h.cx() + h.rx() + 2, y - 20);
        g.setFill(INK);
    }

    /** 梁亭瑄：中等身高、偏瘦，腦後翹起的短馬尾，A 字裙。 */
    private static void tingxuan(GraphicsContext g) {
        Head h = HEADS.get("亭瑄");
        arms(g, 106, 364, 38, 690, 40);
        torso(g, 352, 96, 304, 650, 124, 276);
        poly(g, 124, 646, 276, 646, 310, 832, 90, 832);
        legs(g, 830, 128, 272, 26);
        g.fillRect(180, 280, 40, 80);
        g.save();
        g.translate(284, 196);
        g.rotate(32);
        oval(g, 0, 0, 30, 54);
        g.restore();
        oval(g, 262, 176, 16, 14);
        oval(g, 200, 214, 80, 84);
        head(g, h, false);
    }

    /** 李有成：肩膀寬、刺刺的短髮，耳機掛在脖子上，襯衫沒紮。 */
    private static void acheng(GraphicsContext g) {
        Head h = HEADS.get("阿成");
        arms(g, 64, 352, 6, 708, 50);
        torso(g, 340, 50, 350, 800, 76, 324);
        poly(g, 70, 790, 330, 790, 322, 816, 78, 816);
        legs(g, 812, 84, 316, 16);
        g.fillRect(170, 268, 60, 80);
        oval(g, 140, 316, 24, 28);
        oval(g, 260, 316, 24, 28);
        g.setStroke(INK);
        g.setLineWidth(12);
        g.strokeArc(140, 260, 120, 100, 200, 140, ArcType.OPEN);
        head(g, h, true);
        poly(g, 116, 196, 110, 138, 134, 148, 136, 102, 166, 134, 182, 88, 204, 128, 226, 90, 238, 132,
                268, 104, 266, 148, 292, 140, 284, 196);
    }

    /** 燕韋芸：嬌小、黑長直，化工科的實驗護目鏡推在頭頂。 */
    private static void xiaoyun(GraphicsContext g) {
        Head h = HEADS.get("小芸");
        arms(g, 124, 392, 66, 700, 36);
        g.beginPath();
        g.moveTo(130, 230);
        g.bezierCurveTo(124, 190, 160, 180, 200, 182);
        g.bezierCurveTo(240, 180, 276, 190, 270, 230);
        g.lineTo(296, 470);
        g.lineTo(276, 488);
        g.lineTo(254, 474);
        g.lineTo(230, 490);
        g.lineTo(170, 490);
        g.lineTo(146, 474);
        g.lineTo(124, 488);
        g.lineTo(104, 470);
        g.closePath();
        g.fill();
        torso(g, 382, 116, 284, 660, 136, 264);
        poly(g, 136, 656, 264, 656, 292, 834, 108, 834);
        legs(g, 832, 142, 258, 24);
        g.fillRect(182, 318, 36, 70);
        head(g, h, false);
        g.setStroke(INK);
        g.setLineWidth(12);
        g.strokeArc(130, 178, 140, 60, 10, 160, ArcType.OPEN);
        for (double cx : new double[] {176, 224}) {
            oval(g, cx, 190, 26, 24);
            g.setFill(new LinearGradient(0, 172, 0, 208, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#d8ccff")), new Stop(1, Color.web("#8e7cc3"))));
            oval(g, cx, 190, 17, 15);
            g.setStroke(Color.web("#ffffff", 0.85));
            g.setLineWidth(4);
            g.strokeLine(cx - 8, 196, cx - 2, 184);
            g.setFill(INK);
        }
    }

    /** 王葉聖老師：比學生高、肩膀寬但不厚，旁分的整齊短髮。 */
    private static void teacher(GraphicsContext g) {
        Head h = HEADS.get("王老師");
        arms(g, 72, 340, 14, 700, 46);
        torso(g, 328, 60, 340, 790, 92, 308);
        poly(g, 92, 780, 308, 780, 312, 812, 88, 812);
        legs(g, 810, 90, 310, 16);
        g.fillRect(172, 250, 56, 86);
        head(g, h, true);
        g.beginPath();
        g.moveTo(124, 196);
        g.bezierCurveTo(116, 124, 160, 92, 210, 94);
        g.bezierCurveTo(262, 96, 286, 132, 278, 196);
        g.lineTo(262, 150);
        g.lineTo(140, 150);
        g.closePath();
        g.fill();
        g.beginPath();
        g.moveTo(150, 112);
        g.quadraticCurveTo(170, 84, 214, 90);
        g.quadraticCurveTo(186, 98, 168, 120);
        g.closePath();
        g.fill();
    }

    /** 名冊裡沒有剪影的角色，先用中性的剪影代替。 */
    private static void generic(GraphicsContext g) {
        arms(g, 76, 352, 18, 700, 46);
        torso(g, 340, 66, 334, 790, 96, 304);
        legs(g, 788, 96, 304, 16);
        g.fillRect(176, 270, 48, 80);
        head(g, DEFAULT_HEAD, true);
    }

    /** 「一般」以外的表情都有頭頂符號。 */
    public static boolean hasSymbol(String expr) {
        return EXPRESSIONS.contains(expr) && !expr.equals("一般");
    }

    public static Color symbolColor(String expr) {
        return Color.web(switch (expr) {
            case "笑" -> "#ffd75e";
            case "生氣" -> "#ff5a4e";
            case "害羞" -> "#ff7a9a";
            case "驚訝" -> "#ffc341";
            case "疑惑" -> "#6fe3ff";
            case "冒汗" -> "#8fd6ff";
            case "心動" -> "#ff6f91";
            case "難過" -> "#7f9cff";
            default -> "#d9d4cc";
        });
    }

    /** 符號的霓虹光暈。 */
    public static Effect symbolGlow(String expr, double size) {
        return new DropShadow(BlurType.GAUSSIAN, symbolColor(expr), size * 0.18, 0.3, 0, 0);
    }

    /**
     * 符號中心在單位框裡的位置，都放在頭髮外側：多數在頭的右上方，汗滴在太陽穴旁，⋯⋯在耳朵旁，難過的陰影線在頭的左上方。
     */
    public static double[] symbolAnchor(String person, String expr) {
        Head h = HEADS.getOrDefault(person, DEFAULT_HEAD);
        double side = h.mirrored() ? -1 : 1;
        double cx = h.cx() + MARGIN;
        return switch (expr) {
            case "冒汗" -> new double[] {cx + side * (h.rx() + 30), h.cy() - h.ry() * 0.3};
            case "無言" -> new double[] {cx + side * (h.rx() + 62), h.cy() - 4};
            case "難過" -> new double[] {cx - side * (h.rx() + 44), h.cy() - h.ry() - 6};
            default -> new double[] {cx + side * (h.rx() + 70), h.cy() - h.ry() - 22};
        };
    }

    /** 在邊長 {@code size} 的方框裡畫表情符號，背景透明。先描一圈深色外框，淺色背景上也看得清楚。 */
    public static void paintSymbol(GraphicsContext g, double size, String expr) {
        g.save();
        g.clearRect(0, 0, size, size);
        g.scale(size / 100, size / 100);
        g.setLineCap(StrokeLineCap.ROUND);
        g.setLineJoin(StrokeLineJoin.ROUND);
        Color c = symbolColor(expr);
        for (int pass = 0; pass < 2; pass++) {
            boolean outline = pass == 0;
            g.setStroke(outline ? INK : c);
            g.setFill(c);
            double extra = outline ? 7 : 0;
            switch (expr) {
                case "生氣" -> {
                    g.setLineWidth(10 + extra);
                    g.strokeArc(-40, -40, 80, 80, 285, 60, ArcType.OPEN);
                    g.strokeArc(60, -40, 80, 80, 195, 60, ArcType.OPEN);
                    g.strokeArc(-40, 60, 80, 80, 15, 60, ArcType.OPEN);
                    g.strokeArc(60, 60, 80, 80, 105, 60, ArcType.OPEN);
                }
                case "冒汗" -> {
                    drop(g);
                    filled(g, outline, extra);
                    if (!outline) {
                        g.setStroke(Color.web("#ffffff", 0.9));
                        g.setLineWidth(6);
                        g.strokeLine(38, 74, 36, 60);
                    }
                }
                case "心動" -> {
                    heart(g);
                    filled(g, outline, extra);
                }
                case "笑" -> {
                    star(g, 36, 44, 30);
                    filled(g, outline, extra);
                    star(g, 76, 76, 16);
                    filled(g, outline, extra);
                }
                case "害羞" -> {
                    g.setLineWidth(7 + extra);
                    for (int i = 0; i < 3; i++) {
                        g.strokeArc(18 + i * 22, 30 + (i % 2) * 8, 22, 40, 100, 160, ArcType.OPEN);
                    }
                }
                case "難過" -> {
                    g.setLineWidth(7 + extra);
                    for (int i = 0; i < 4; i++) {
                        double x = 20 + i * 20;
                        g.beginPath();
                        g.moveTo(x, 10);
                        g.bezierCurveTo(x + 8, 30, x - 8, 50, x, 70 + (i % 2) * 14);
                        g.stroke();
                    }
                }
                case "無言" -> {
                    for (int i = 0; i < 3; i++) {
                        g.beginPath();
                        g.arc(20 + i * 30, 50, 9, 9, 0, 360);
                        filled(g, outline, extra);
                    }
                }
                default -> {
                    g.setFont(Font.font("Source Han Sans TC", FontWeight.BLACK, 86));
                    g.setTextAlign(TextAlignment.CENTER);
                    String text = expr.equals("疑惑") ? "?" : "!!";
                    if (outline) {
                        g.setLineWidth(extra);
                        g.strokeText(text, 50, 84);
                    } else {
                        g.fillText(text, 50, 84);
                    }
                }
            }
        }
        g.restore();
    }

    /** 外框那一輪只描邊，第二輪才填色。 */
    private static void filled(GraphicsContext g, boolean outline, double extra) {
        if (outline) {
            g.setLineWidth(extra);
            g.stroke();
        } else {
            g.fill();
        }
    }

    private static void drop(GraphicsContext g) {
        g.beginPath();
        g.moveTo(50, 6);
        g.bezierCurveTo(72, 40, 82, 58, 76, 74);
        g.bezierCurveTo(68, 96, 32, 96, 24, 74);
        g.bezierCurveTo(18, 58, 28, 40, 50, 6);
        g.closePath();
    }

    private static void heart(GraphicsContext g) {
        g.beginPath();
        g.moveTo(50, 88);
        g.bezierCurveTo(8, 60, 6, 28, 28, 18);
        g.bezierCurveTo(40, 12, 50, 22, 50, 32);
        g.bezierCurveTo(50, 22, 60, 12, 72, 18);
        g.bezierCurveTo(94, 28, 92, 60, 50, 88);
        g.closePath();
    }

    private static void star(GraphicsContext g, double cx, double cy, double r) {
        g.beginPath();
        for (int i = 0; i < 8; i++) {
            double a = Math.PI / 4 * i - Math.PI / 2;
            double rr = i % 2 == 0 ? r : r * 0.32;
            double x = cx + Math.cos(a) * rr;
            double y = cy + Math.sin(a) * rr;
            if (i == 0) {
                g.moveTo(x, y);
            } else {
                g.lineTo(x, y);
            }
        }
        g.closePath();
    }
}
