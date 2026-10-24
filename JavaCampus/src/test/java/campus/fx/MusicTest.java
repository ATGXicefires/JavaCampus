package campus.fx;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MusicTest {

    private final Fader fader = new Fader(false);
    private final List<Boolean> musicOn = new ArrayList<>();
    private final Music music = new Music(fader, musicOn::add);
    private final Map<Music.Track, FakeClip> clips = new EnumMap<>(Music.Track.class);

    @Test
    void everyTrackRendersAnAudibleLoopOfTheRightLength() {
        for (Music.Track t : Music.Track.values()) {
            float[] samples = Music.render(t);
            double seconds = samples.length / Synth.RATE;
            assertTrue(seconds >= 29 && seconds <= 61, t + " 長度 " + seconds);
            double peak = 0;
            double energy = 0;
            for (float v : samples) {
                assertTrue(Float.isFinite(v), t + " 有不是數字的取樣");
                peak = Math.max(peak, Math.abs(v));
                energy += v * v;
            }
            assertTrue(peak > 0.8 && peak <= 0.86, t + " 峰值 " + peak);
            assertTrue(Math.sqrt(energy / samples.length) > 0.02, t + " 幾乎沒有聲音");
        }
    }

    @Test
    void noteNames() {
        assertEquals(60, Music.Score.midi("C4"));
        assertEquals(70, Music.Score.midi("Bb4"));
        assertEquals(78, Music.Score.midi("F#5"));
        assertEquals(84, Music.Score.midi("C6"));
    }

    private FakeClip render(Music.Track t) {
        FakeClip c = new FakeClip();
        clips.put(t, c);
        assertTrue(music.rendered(t, c.clip));
        return c;
    }

    private void renderAll() {
        for (Music.Track t : Music.Track.values()) {
            render(t);
        }
    }

    private void run(double seconds) {
        for (long i = Math.round(seconds * 1000 / Fader.TICK_MS); i > 0; i--) {
            fader.tick();
            long audible = clips.values().stream().filter(FakeClip::audible).count();
            assertTrue(audible <= 1, "同時有 " + audible + " 首在出聲");
        }
    }

    private Set<Music.Track> running() {
        Set<Music.Track> out = EnumSet.noneOf(Music.Track.class);
        clips.forEach((t, c) -> {
            if (c.running) {
                out.add(t);
            }
        });
        return out;
    }

    @Test
    void switchingTracksFadesTheOldOneOutBeforeTheNextStarts() {
        music.setVolume(1);
        renderAll();
        music.play(Music.Track.TITLE);
        run(1);
        music.play(Music.Track.DAILY);
        run(0.4);
        assertEquals(Set.of(Music.Track.TITLE), running());
        run(0.5);
        assertEquals(Set.of(Music.Track.DAILY), running());
        assertEquals(0, clips.get(Music.Track.DAILY).frame, "新的一首從頭開始");
        run(1);
        assertEquals(20 * Math.log10(0.45), clips.get(Music.Track.DAILY).gain.getValue(), 0.01);
    }

    @Test
    void rapidSwitchesLeaveOnlyTheLastTrack() {
        music.setVolume(1);
        renderAll();
        music.play(Music.Track.TITLE);
        run(1);
        music.play(Music.Track.DAILY);
        run(0.1);
        music.play(Music.Track.COMEDY);
        run(0.1);
        music.play(Music.Track.HEART);
        run(0.1);
        music.play(Music.Track.RAIN);
        run(1.5);
        assertEquals(Set.of(Music.Track.RAIN), running());
        assertEquals(0, clips.get(Music.Track.DAILY).starts);
        assertEquals(0, clips.get(Music.Track.COMEDY).starts);
        assertEquals(0, clips.get(Music.Track.HEART).starts);
    }

    @Test
    void aLateRenderStartsOnlyIfStillWantedAndAfterThePreviousTrackStops() {
        music.setVolume(1);
        render(Music.Track.TITLE);
        music.play(Music.Track.TITLE);
        run(1);
        music.play(Music.Track.DAILY);
        music.play(Music.Track.COMEDY);
        FakeClip daily = render(Music.Track.DAILY);
        run(0.2);
        FakeClip comedy = render(Music.Track.COMEDY);
        assertFalse(comedy.running, "前一首還在淡出");
        run(1.5);
        assertEquals(Set.of(Music.Track.COMEDY), running());
        assertEquals(0, daily.starts);
    }

    @Test
    void stoppingWhileTheNextTrackWaitsStartsNothing() {
        music.setVolume(1);
        renderAll();
        music.play(Music.Track.TITLE);
        run(1);
        music.play(Music.Track.DAILY);
        run(0.2);
        music.stop();
        run(2);
        assertEquals(Set.of(), running());
        assertEquals(0, clips.get(Music.Track.DAILY).starts);
    }

    @Test
    void ambienceIsToldWhetherATrackCanActuallyBeHeard() {
        music.setVolume(1);
        music.play(Music.Track.TITLE);
        assertFalse(musicOn.getLast(), "還沒合成好");
        render(Music.Track.TITLE);
        assertTrue(musicOn.getLast());
        music.setVolume(0);
        assertFalse(musicOn.getLast(), "音樂音量調到 0");
        music.setVolume(1);
        assertTrue(musicOn.getLast());
        music.play(Music.Track.DAILY);
        assertFalse(musicOn.getLast(), "換到還沒合成好的曲子");
        render(Music.Track.DAILY);
        assertTrue(musicOn.getLast());
        music.stop();
        assertFalse(musicOn.getLast());
    }

    @Test
    void audibleTrack() {
        Set<Music.Track> rendered = EnumSet.of(Music.Track.TITLE);
        assertEquals(Music.Track.TITLE, Music.audible(Music.Track.TITLE, 0.3, rendered));
        assertNull(Music.audible(Music.Track.TITLE, 0, rendered));
        assertNull(Music.audible(Music.Track.DAILY, 0.3, rendered));
        assertNull(Music.audible(null, 0.3, rendered));
    }
}
