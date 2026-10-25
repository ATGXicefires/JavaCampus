package campus.art;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BackgroundsTest {

    @Test
    void everyVariantTellsThePhoneWhatTimeItIs() {
        Backgrounds.NAMES.forEach((name, variants) -> variants.forEach(v ->
                assertNotNull(Backgrounds.clock(name, v), name + " " + v + " 沒有對應的時間")));
    }

    @Test
    void theNightRoomShowsANightTime() {
        assertEquals("22:10", Backgrounds.clock("亭瑄的房間", "夜"));
        assertEquals("22:10", Backgrounds.clock("亭瑄的房間", null));
        assertEquals("12:20", Backgrounds.clock(null, null));
    }
}
