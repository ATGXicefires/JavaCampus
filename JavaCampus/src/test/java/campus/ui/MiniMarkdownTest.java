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
import java.util.List;

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

    @Test
    void spacesThatHaveToBeTypedAreDrawn() {
        assertEquals("第␣1␣遍：罰寫", MiniMarkdown.visibleSpaces("第 1 遍：罰寫", true));
        assertEquals("i <= times", MiniMarkdown.visibleSpaces("i <= times", true));
        // 帶中文說明的程式碼不是輸出
        assertEquals("substring(開始, 結束)", MiniMarkdown.visibleSpaces("substring(開始, 結束)", true));
        assertEquals("this.欄位 = 參數", MiniMarkdown.visibleSpaces("this.欄位 = 參數", true));
        assertEquals("answers[第幾次 - 1]", MiniMarkdown.visibleSpaces("answers[第幾次 - 1]", true));
        assertEquals("\"第␣\" + i + \"␣遍\"", MiniMarkdown.visibleSpaces("\"第 \" + i + \" 遍\"", true));
        // 程式區塊的縮排和中文說明不是要印的字
        String block = "for (int i = 1; i <= 3; i++) {\n    要重複做的事\n}";
        assertEquals(block, MiniMarkdown.visibleSpaces(block, false));

        TextFlow flow = MiniMarkdown.inline("印出 `第 1 遍`，再接 `i + 1`");
        List<Text> marks = flow.getChildren().stream().map(Text.class::cast)
                .filter(t -> t.getStyleClass().contains("md-space")).toList();
        assertEquals(2, marks.size());
        assertTrue(marks.stream().allMatch(t -> t.getText().equals("␣") && t.getStyleClass().contains("md-code")));
    }

    private static void assertAlt(Node node) {
        TextFlow flow = assertInstanceOf(TextFlow.class, node);
        assertTrue(flow.getStyleClass().contains("md-img-missing"));
        assertEquals("系統架構圖", ((Text) flow.getChildren().getFirst()).getText());
    }
}
