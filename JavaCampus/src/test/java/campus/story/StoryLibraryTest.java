package campus.story;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class StoryLibraryTest {

    @Test
    void chaptersWithoutADirAreListedButNotPlayable(@TempDir Path root) throws IOException {
        Files.writeString(root.resolve("chapters.json"), """
                {"chapters": [
                  {"id": "ch01", "number": 1, "title": "編譯錯誤", "dir": "ch01", "progress": 5},
                  {"id": "ch02", "number": 2, "title": "Hello World"}
                ]}
                """, UTF_8);
        Path dir = Files.createDirectory(root.resolve("ch01"));
        for (String suffix : List.of("common", "senpai", "kouhai")) {
            Files.writeString(dir.resolve("ch01-" + suffix + ".md"), "[開場]\n（開學第一週。）\n", UTF_8);
        }

        StoryLibrary library = StoryLibrary.load(root);

        assertEquals(List.of("ch01"), library.chapters().stream().map(Chapter::id).toList());
        assertEquals(2, library.contents().size());
        assertSame(library.chapters().getFirst(), library.contents().get(0).chapter());
        assertEquals("Hello World", library.contents().get(1).title());
        assertNull(library.contents().get(1).chapter());
        assertNull(library.next(library.chapters().getFirst()));
        assertEquals(root.resolve("docs"), library.docDir());
    }
}
