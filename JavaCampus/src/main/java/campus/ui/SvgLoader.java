package campus.ui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Translate;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 把一小部分 SVG 轉成 JavaFX 節點。JavaFX 不能直接顯示 SVG 檔，文件裡又只有方框圖，不值得為此引入 WebView。
 * 支援 rect、line、polyline、polygon、path、circle、ellipse、text 和用 translate 平移的 g；
 * 沒有 marker，箭頭要直接畫成 polygon。
 */
public final class SvgLoader {

    /**
     * @param root   畫好的節點，座標和 SVG 的 viewBox 相同
     * @param width  viewBox 的寬，縮放時以此為準
     * @param height viewBox 的高
     */
    public record Drawing(Group root, double width, double height) {}

    private static final String DEFAULT_FONT = "Source Han Sans TC";
    /** 寫在 svg 或 g 上會傳給裡面每個圖形的屬性。 */
    private static final Set<String> INHERITED = Set.of("fill", "stroke", "stroke-width", "stroke-dasharray",
            "font-family", "font-size", "font-weight", "text-anchor");
    private static final Pattern NUMBER = Pattern.compile("[-+]?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][-+]?\\d+)?");
    private static final Pattern TRANSLATE = Pattern.compile("\\s*translate\\(([^)]*)\\)\\s*");

    private SvgLoader() {}

    public static Drawing load(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            return parse(in);
        }
    }

    /** @throws IllegalArgumentException 用了不支援的寫法，例如 translate 以外的 transform */
    public static Drawing parse(InputStream in) throws IOException {
        Element svg;
        try {
            svg = builder().parse(in).getDocumentElement();
        } catch (SAXException e) {
            throw new IOException("SVG 格式錯誤：" + e.getMessage(), e);
        }
        if (!name(svg).equals("svg")) {
            throw new IOException("最外層要是 <svg>，不是 <" + name(svg) + ">");
        }
        double[] box = viewBox(svg);
        Group content = new Group();
        draw(svg, inherit(Map.of(), svg), content);
        if (box[0] != 0 || box[1] != 0) {
            content.getTransforms().add(new Translate(-box[0], -box[1]));
        }
        return new Drawing(new Group(content), box[2], box[3]);
    }

    private static DocumentBuilder builder() {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        f.setXIncludeAware(false);
        f.setExpandEntityReferences(false);
        try {
            // 不讀 DTD、不展開外部實體，免得一個 SVG 就能讀到本機的其他檔案（XXE）
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature("http://xml.org/sax/features/external-general-entities", false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            DocumentBuilder b = f.newDocumentBuilder();
            // 預設的錯誤處理會把每個錯誤印到主控台；改成只拋出例外
            b.setErrorHandler(new DefaultHandler());
            return b;
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("JDK 內建的 XML 解析器應該支援這些設定", e);
        }
    }

    /** viewBox 的 x、y、寬、高；沒寫 viewBox 就用 width、height。 */
    private static double[] viewBox(Element svg) throws IOException {
        if (svg.hasAttribute("viewBox")) {
            List<Double> v = numbers(svg.getAttribute("viewBox"));
            if (v.size() == 4 && v.get(2) > 0 && v.get(3) > 0) {
                return new double[]{v.get(0), v.get(1), v.get(2), v.get(3)};
            }
        } else if (svg.hasAttribute("width") && svg.hasAttribute("height")) {
            return new double[]{0, 0, number(svg.getAttribute("width")), number(svg.getAttribute("height"))};
        }
        throw new IOException("<svg> 要寫 viewBox=\"0 0 寬 高\"");
    }

    private static void draw(Element parent, Map<String, String> inherited, Group into) {
        for (org.w3c.dom.Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (!(n instanceof Element e)) {
                continue;
            }
            Map<String, String> style = inherit(inherited, e);
            Node node = switch (name(e)) {
                case "g" -> {
                    Group g = new Group();
                    draw(e, style, g);
                    yield g;
                }
                case "rect" -> new Rectangle(attr(e, "x"), attr(e, "y"), attr(e, "width"), attr(e, "height"));
                case "line" -> new Line(attr(e, "x1"), attr(e, "y1"), attr(e, "x2"), attr(e, "y2"));
                case "polyline" -> new Polyline(points(e));
                case "polygon" -> new Polygon(points(e));
                case "path" -> {
                    SVGPath p = new SVGPath();
                    p.setContent(e.getAttribute("d"));
                    yield p;
                }
                case "circle" -> new Circle(attr(e, "cx"), attr(e, "cy"), attr(e, "r"));
                case "ellipse" -> new Ellipse(attr(e, "cx"), attr(e, "cy"), attr(e, "rx"), attr(e, "ry"));
                case "text" -> text(e, style);
                // title、desc、defs 之類本來就不會畫出來的元素
                default -> null;
            };
            if (node == null) {
                continue;
            }
            if (node instanceof Shape s) {
                paint(s, style);
            }
            if (e.hasAttribute("transform")) {
                node.getTransforms().add(translate(e.getAttribute("transform")));
            }
            into.getChildren().add(node);
        }
    }

    private static Map<String, String> inherit(Map<String, String> inherited, Element e) {
        Map<String, String> style = new HashMap<>(inherited);
        for (String key : INHERITED) {
            if (e.hasAttribute(key)) {
                style.put(key, e.getAttribute(key));
            }
        }
        return style;
    }

    /** 沒寫的值照 SVG 的預設：填黑色、不描邊、線寬 1。 */
    private static void paint(Shape s, Map<String, String> style) {
        s.setFill(color(style.getOrDefault("fill", "black")));
        s.setStroke(color(style.getOrDefault("stroke", "none")));
        s.setStrokeWidth(number(style.getOrDefault("stroke-width", "1")));
        // JavaFX 預設方頭會多出半個線寬，SVG 預設是平頭，線接到箭頭才不會戳出來
        s.setStrokeLineCap(StrokeLineCap.BUTT);
        String dash = style.get("stroke-dasharray");
        if (dash != null && !dash.equals("none")) {
            s.getStrokeDashArray().setAll(numbers(dash));
        }
    }

    private static Paint color(String value) {
        return value.equals("none") ? null : Color.web(value.strip());
    }

    /** JavaFX 的 Text 沒有 text-anchor，要量出字寬自己往左移。 */
    private static Text text(Element e, Map<String, String> style) {
        Text t = new Text(e.getTextContent().strip().replaceAll("\\s+", " "));
        t.setFont(Font.font(family(style.get("font-family")), weight(style.get("font-weight")),
                number(style.getOrDefault("font-size", "16"))));
        double shift = switch (style.getOrDefault("text-anchor", "start")) {
            case "middle" -> 0.5;
            case "end" -> 1;
            default -> 0;
        };
        t.setX(attr(e, "x") - t.getLayoutBounds().getWidth() * shift);
        t.setY(attr(e, "y"));
        return t;
    }

    /** font-family 可以列好幾個備用字型，這裡只用第一個，字型都隨遊戲附帶。 */
    private static String family(String value) {
        if (value == null) {
            return DEFAULT_FONT;
        }
        String first = value.split(",")[0].strip().replaceAll("^['\"]|['\"]$", "");
        return first.isEmpty() ? DEFAULT_FONT : first;
    }

    private static FontWeight weight(String value) {
        if (value == null) {
            return FontWeight.NORMAL;
        }
        if (value.matches("\\d+")) {
            return FontWeight.findByWeight(Integer.parseInt(value));
        }
        FontWeight w = FontWeight.findByName(value.strip());
        return w != null ? w : FontWeight.NORMAL;
    }

    private static Translate translate(String value) {
        Matcher m = TRANSLATE.matcher(value);
        if (!m.matches()) {
            throw new IllegalArgumentException("transform 只支援 translate(x, y)：" + value);
        }
        List<Double> xy = numbers(m.group(1));
        if (xy.isEmpty() || xy.size() > 2) {
            throw new IllegalArgumentException("translate 要寫一個或兩個數字：" + value);
        }
        return new Translate(xy.get(0), xy.size() > 1 ? xy.get(1) : 0);
    }

    private static double[] points(Element e) {
        List<Double> list = numbers(e.getAttribute("points"));
        double[] out = new double[list.size() - list.size() % 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = list.get(i);
        }
        return out;
    }

    /** 沒寫的座標和尺寸是 0，和 SVG 一樣。 */
    private static double attr(Element e, String name) {
        return e.hasAttribute(name) ? number(e.getAttribute(name)) : 0;
    }

    private static double number(String value) {
        return Double.parseDouble(value.strip().replaceFirst("px$", ""));
    }

    private static List<Double> numbers(String value) {
        List<Double> out = new ArrayList<>();
        Matcher m = NUMBER.matcher(value);
        while (m.find()) {
            out.add(Double.parseDouble(m.group()));
        }
        return out;
    }

    private static String name(Element e) {
        return e.getLocalName() != null ? e.getLocalName() : e.getNodeName();
    }
}
