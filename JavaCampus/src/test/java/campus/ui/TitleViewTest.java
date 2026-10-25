package campus.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TitleViewTest {

    @Test
    void classroomStaysEmptyUntilTheFirstChapterIsCleared() {
        assertNull(TitleView.poseAfter(0));
    }

    @Test
    void eachPoseLastsUntilTheNextOneIsReached() {
        assertEquals(1, TitleView.poseAfter(1).afterChapter());
        assertEquals(1, TitleView.poseAfter(5).afterChapter());
        assertEquals(6, TitleView.poseAfter(7).afterChapter());
        assertEquals(11, TitleView.poseAfter(12).afterChapter());
    }
}
