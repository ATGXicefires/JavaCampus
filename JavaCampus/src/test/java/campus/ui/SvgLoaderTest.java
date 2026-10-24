package campus.ui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Translate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SvgLoaderTest {

    /** 遊戲啟動時才會載入附帶的字型，測試要自己載，字型名稱和粗細才查得到。 */
    @BeforeAll
    static void loadFonts() {
        for (String f : List.of("SourceHanSans/SourceHanSansTC-Regular.otf", "SourceHanSans/SourceHanSansTC-Bold.otf",
                "MapleMono/MapleMono-NF-CN-Regular.ttf")) {
            assertNotNull(Font.loadFont(Path.of("fonts", f).toUri().toString(), 12), f);
        }
    }

    private static SvgLoader.Drawing parse(String svg) throws IOException {
        return SvgLoader.parse(new ByteArrayInputStream(svg.getBytes(UTF_8)));
    }

    /** 圖形放在 root 裡面那一層，viewBox 的平移掛在那一層上。 */
    private static List<Node> shapes(SvgLoader.Drawing d) {
        return ((Group) d.root().getChildren().getFirst()).getChildren();
    }

    @Test
    void mapsEverySupportedElementToItsShape() throws IOException {
        SvgLoader.Drawing d = parse("""
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 200">
                  <title>不畫出來</title>
                  <rect x="4" y="6" width="120" height="40" fill="#ffffff" stroke="#2b333d" stroke-width="1.5"/>
                  <line x1="0" y1="10" x2="50" y2="10" stroke="red" stroke-dasharray="4 3"/>
                  <polyline points="1,2 3,4 5,6" fill="none" stroke="#000"/>
                  <polygon points="10,0 0,5 10,10"/>
                  <path d="M0 0 L10 10"/>
                  <circle cx="5" cy="6" r="7"/>
                  <ellipse cx="1" cy="2" rx="3" ry="4"/>
                </svg>
                """);

        assertEquals(400, d.width());
        assertEquals(200, d.height());
        List<Node> nodes = shapes(d);
        assertEquals(7, nodes.size());

        Rectangle rect = assertInstanceOf(Rectangle.class, nodes.get(0));
        assertEquals(4, rect.getX());
        assertEquals(6, rect.getY());
        assertEquals(120, rect.getWidth());
        assertEquals(40, rect.getHeight());
        assertEquals(Color.WHITE, rect.getFill());
        assertEquals(Color.web("#2b333d"), rect.getStroke());
        assertEquals(1.5, rect.getStrokeWidth());

        Line line = assertInstanceOf(Line.class, nodes.get(1));
        assertEquals(50, line.getEndX());
        assertEquals(Color.RED, line.getStroke());
        assertEquals(List.of(4.0, 3.0), line.getStrokeDashArray());

        Polyline polyline = assertInstanceOf(Polyline.class, nodes.get(2));
        assertEquals(List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0), polyline.getPoints());
        assertNull(polyline.getFill());

        Polygon polygon = assertInstanceOf(Polygon.class, nodes.get(3));
        assertEquals(6, polygon.getPoints().size());
        assertEquals(Color.BLACK, polygon.getFill(), "沒寫 fill 時和 SVG 一樣填黑色");
        assertNull(polygon.getStroke(), "沒寫 stroke 時和 SVG 一樣不描邊");

        assertEquals("M0 0 L10 10", assertInstanceOf(SVGPath.class, nodes.get(4)).getContent());
        assertEquals(7, assertInstanceOf(Circle.class, nodes.get(5)).getRadius());
        assertEquals(4, assertInstanceOf(Ellipse.class, nodes.get(6)).getRadiusY());
    }

    @Test
    void groupsTranslateAndPassTheirStyleDown() throws IOException {
        SvgLoader.Drawing d = parse("""
                <svg viewBox="0 0 100 100" font-family="'Maple Mono NF CN', monospace">
                  <g transform="translate(266, 4)" fill="#4b535d" font-size="11.5">
                    <text x="10" y="20">JDBC / SQLite</text>
                    <text x="10" y="40" font-weight="bold" font-family="Source Han Sans TC">Service Layer</text>
                  </g>
                </svg>
                """);

        Group g = assertInstanceOf(Group.class, shapes(d).getFirst());
        Translate t = assertInstanceOf(Translate.class, g.getTransforms().getFirst());
        assertEquals(266, t.getX());
        assertEquals(4, t.getY());

        Text mono = assertInstanceOf(Text.class, g.getChildren().get(0));
        assertEquals("JDBC / SQLite", mono.getText());
        assertEquals(Color.web("#4b535d"), mono.getFill());
        assertEquals(11.5, mono.getFont().getSize());
        assertEquals("Maple Mono NF CN", mono.getFont().getFamily());
        assertEquals(10, mono.getX());
        assertEquals(20, mono.getY());

        Text bold = assertInstanceOf(Text.class, g.getChildren().get(1));
        assertEquals("Source Han Sans TC", bold.getFont().getFamily());
        assertEquals("Bold", bold.getFont().getStyle());
    }

    @Test
    void textAnchorShiftsTheTextByItsWidth() throws IOException {
        SvgLoader.Drawing d = parse("""
                <svg viewBox="0 0 200 50">
                  <text x="100" y="20">Logger</text>
                  <text x="100" y="20" text-anchor="middle">Logger</text>
                  <text x="100" y="20" text-anchor="end">Logger</text>
                </svg>
                """);

        List<Node> nodes = shapes(d);
        double width = ((Text) nodes.get(0)).getLayoutBounds().getWidth();
        assertTrue(width > 0);
        assertEquals(100, ((Text) nodes.get(0)).getX());
        assertEquals(100 - width / 2, ((Text) nodes.get(1)).getX(), 1e-9);
        assertEquals(100 - width, ((Text) nodes.get(2)).getX(), 1e-9);
    }

    @Test
    void aViewBoxThatDoesNotStartAtZeroIsShiftedBack() throws IOException {
        SvgLoader.Drawing d = parse("<svg viewBox=\"10 20 30 40\"><rect width=\"1\" height=\"1\"/></svg>");

        Translate t = assertInstanceOf(Translate.class, d.root().getChildren().getFirst().getTransforms().getFirst());
        assertEquals(-10, t.getX());
        assertEquals(-20, t.getY());
        assertEquals(30, d.width());
    }

    @Test
    void refusesDoctypesSoExternalEntitiesCannotBeRead() {
        String xxe = """
                <?xml version="1.0"?>
                <!DOCTYPE svg [<!ENTITY secret SYSTEM "file:///C:/Windows/win.ini">]>
                <svg viewBox="0 0 10 10"><text>&secret;</text></svg>
                """;
        assertThrows(IOException.class, () -> parse(xxe));
    }

    @Test
    void rejectsWhatItCannotDraw() {
        assertThrows(IOException.class, () -> parse("<svg><rect/></svg>"), "沒有 viewBox");
        assertThrows(IOException.class, () -> parse("<html/>"));
        assertThrows(IOException.class, () -> parse("<svg viewBox=\"0 0 1 1\">"));
        assertThrows(IllegalArgumentException.class,
                () -> parse("<svg viewBox=\"0 0 1 1\"><g transform=\"rotate(45)\"/></svg>"));
    }
}
