package campus.ui;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiniMarkdownTest {

    @Test
    void anImageParagraphScalesTheSvgToTheAvailableWidth(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("圖.svg"),
                "<svg viewBox=\"0 0 400 100\"><rect width=\"400\" height=\"100\"/></svg>", UTF_8);

        VBox box = MiniMarkdown.render("**2. 系統架構圖**\n\n![系統架構圖](圖.svg)\n\n後面的段落", dir);

        assertEquals(3, box.getChildren().size());
        MiniMarkdown.Picture picture = assertInstanceOf(MiniMarkdown.Picture.class, box.getChildren().get(1));
        assertTrue(picture.getStyleClass().contains("md-img"));
        double insets = picture.snappedTopInset() + picture.snappedBottomInset();
        assertEquals(50 + insets, picture.prefHeight(200 + picture.snappedLeftInset() + picture.snappedRightInset()));
    }

    @Test
    void aMissingImageFallsBackToItsAltText(@TempDir Path dir) {
        assertAlt(MiniMarkdown.render("![系統架構圖](沒有這張.svg)", dir).getChildren().getFirst());
        assertAlt(MiniMarkdown.render("![系統架構圖](圖.svg)").getChildren().getFirst());
    }

    private static void assertAlt(Node node) {
        TextFlow flow = assertInstanceOf(TextFlow.class, node);
        assertTrue(flow.getStyleClass().contains("md-img-missing"));
        assertEquals("系統架構圖", ((Text) flow.getChildren().getFirst()).getText());
    }
}
