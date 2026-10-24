package campus.fx;

import javax.sound.sampled.Clip;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import static campus.fx.Synth.decay;
import static campus.fx.Synth.envelope;
import static campus.fx.Synth.hz;
import static campus.fx.Synth.sine;

/**
 * 用程式合成的背景音樂。每首曲子是一段 30～60 秒的循環，啟動後在背景執行緒依序合成，
 * 標題曲最先做好；還沒合成完就被要求播放的曲子，做好時會自動淡入。
 * 一次只聽得到一首：換曲時前一首完全淡出才開始下一首。
 */
public final class Music implements AutoCloseable {

    public enum Track {
        TITLE("放學後的電腦教室"), DAILY("Debug 日常"), COMEDY("吐槽模式"), HEART("心跳超出預期值"), RAIN("窗外的雨");

        public final String scriptName;

        Track(String scriptName) {
            this.scriptName = scriptName;
        }

        public static Optional<Track> byScriptName(String name) {
            for (Track t : values()) {
                if (t.scriptName.equals(name)) {
                    return Optional.of(t);
                }
            }
            return Optional.empty();
        }
    }

    private static final double LEVEL = 0.45;
    private static final double SWITCH_OUT = 0.7;
    private static final double SWITCH_IN = 0.5;

    private final Fader fader;
    private final Consumer<Boolean> musicOn;
    private final Map<Track, Clip> clips = new EnumMap<>(Track.class);
    private final Thread renderer;
    private Track wanted;
    private double volume = 0.2;
    private volatile boolean closed;

    /** {@code musicOn} 在「真的有曲子在播」變化時收到通知；背景合成要等 {@link #create} 才開始。 */
    Music(Fader fader, Consumer<Boolean> musicOn) {
        this.fader = fader;
        this.musicOn = musicOn;
        renderer = new Thread(this::renderAll, "music-renderer");
        renderer.setDaemon(true);
        renderer.setPriority(Thread.MIN_PRIORITY);
    }

    /** 和音效共用淡入淡出的執行緒，有曲子在播時環境音會讓位；音效停用時音樂也不會有聲音，但不影響遊戲。 */
    public static Music create(Sfx sfx) {
        Music m = new Music(sfx.fader(), sfx::setMusicOn);
        m.renderer.start();
        return m;
    }

    public synchronized void play(Track t) {
        if (t == wanted) {
            return;
        }
        wanted = t;
        apply(SWITCH_IN, SWITCH_OUT, true);
    }

    public synchronized void stop() {
        wanted = null;
        apply(SWITCH_IN, 1.5, false);
    }

    /** 主音量乘上音樂音量，0 到 1。 */
    public synchronized void setVolume(double v) {
        volume = v * LEVEL;
        apply(0.05, 0.05, false);
    }

    /** 其他曲子全部淡出；要播的那首等它們都停掉才從 Fader 開始，所以快速連續換曲也只會留下最後一首。 */
    private synchronized void apply(double in, double out, boolean restart) {
        Track on = audible(wanted, volume, clips.keySet());
        Clip next = on == null ? null : clips.get(on);
        List<Clip> others = new ArrayList<>();
        for (Clip c : clips.values()) {
            if (c != next) {
                others.add(c);
                fader.fadeTo(c, 0, out);
            }
        }
        if (next != null) {
            if (restart && !fader.isAudible(next)) {
                next.setFramePosition(0);
            }
            fader.fadeTo(next, volume, in, others);
        }
        musicOn.accept(next != null);
    }

    /** 真的會出聲的曲子：有人要、音量大於 0、而且已經合成好；都不是就回傳 null。 */
    static Track audible(Track wanted, double volume, Set<Track> rendered) {
        return wanted != null && volume > 0 && rendered.contains(wanted) ? wanted : null;
    }

    /** 背景合成好一首；只有仍然被要求的曲子會淡入，而且照樣等前一首停掉。關閉後回傳 false。 */
    synchronized boolean rendered(Track t, Clip c) {
        if (closed) {
            c.close();
            return false;
        }
        clips.put(t, c);
        if (t == wanted) {
            apply(1.0, SWITCH_OUT, true);
        }
        return true;
    }

    private void renderAll() {
        List<Track> todo = new ArrayList<>(List.of(Track.values()));
        while (!todo.isEmpty() && !closed) {
            Track next;
            synchronized (this) {
                next = todo.contains(wanted) ? wanted : todo.getFirst();
            }
            todo.remove(next);
            try {
                if (!rendered(next, Synth.open(render(next)))) {
                    return;
                }
            } catch (Exception | LinkageError e) {
                System.err.println("背景音樂停用：" + e.getMessage());
                return;
            }
        }
    }

    @Override
    public synchronized void close() {
        closed = true;
        renderer.interrupt();
        clips.values().forEach(Clip::close);
    }

    static float[] render(Track t) {
        return switch (t) {
            case TITLE -> title();
            case DAILY -> daily();
            case COMEDY -> comedy();
            case HEART -> heart();
            case RAIN -> rain();
        };
    }

    /** 放學後的電腦教室：電鋼琴和弦加音樂盒主旋律。 */
    private static float[] title() {
        Score s = new Score(90, 64);
        String[] chords = {"C", "Am", "F", "G", "C", "Am", "F", "G", "F", "G", "Em", "Am", "F", "G", "C", "C"};
        for (int bar = 0; bar < chords.length; bar++) {
            double at = bar * 4;
            for (int n : s.voicing(chords[bar])) {
                s.ep(at, 3.6, n, 0.55);
                s.ep(at + 2, 1.8, n, 0.25);
            }
            s.bass(at, 1.6, s.root(chords[bar]), 0.6);
            s.bass(at + 2, 1.6, s.root(chords[bar]), 0.45);
        }
        s.melody(0, "E5:1 G5:1 A5:1 G5:1 | E5:2 C5:1 D5:1 | C5:1 D5:1 E5:1 F5:1 | D5:3 -:1"
                + " | E5:1 G5:1 C6:1 B5:1 | A5:2 G5:1 E5:1 | F5:1 E5:1 D5:1 C5:1 | D5:2 G4:1 B4:1"
                + " | A4:1 C5:1 F5:2 | G5:1 F5:1 D5:2 | E5:1 G5:1 B5:2 | A5:3 -:1"
                + " | F5:1 A5:1 C6:1 A5:1 | G5:1 D5:1 B4:1 D5:1 | C5:4 | -:2 G4:1 B4:1", s::box, 0.7);
        return s.mix();
    }

    /** Debug 日常：撥弦琶音、柔和低音和輕輕的沙鈴，後半段才有一點旋律。 */
    private static float[] daily() {
        Score s = new Score(100, 64);
        String[] chords = {"F", "Dm", "Bb", "C", "F", "Dm", "Bb", "C", "Bb", "C", "Am", "Dm", "Gm", "C", "F", "F"};
        int[] pattern = {0, 2, 3, 1, 4, 3, 2, 5};
        for (int bar = 0; bar < chords.length; bar++) {
            double at = bar * 4;
            int[] v = s.voicing(chords[bar]);
            int[] tones = {v[0], v[1], v[2], v[0] + 12, v[1] + 12, v[2] + 12};
            for (int i = 0; i < 8; i++) {
                s.pluck(at + i * 0.5, 0.5, tones[pattern[i]], i % 2 == 0 ? 0.5 : 0.35);
                s.shaker(at + i * 0.5 + 0.5, i % 2 == 0 ? 0.35 : 0.2);
            }
            for (int n : v) {
                s.pad(at, 4, n, 0.25);
            }
            int root = s.root(chords[bar]);
            s.bass(at, 1, root, 0.6);
            s.bass(at + 1.5, 0.5, root, 0.35);
            s.bass(at + 2, 1, root + 7, 0.45);
        }
        s.melody(0, "-:32 | D5:1 F5:1 Bb5:2 | C6:1 Bb5:1 G5:2 | A5:1 C6:1 E5:2 | F5:1 A5:1 D5:2"
                + " | Bb4:1 D5:1 G5:2 | E5:2 G5:1 Bb5:1 | A5:4 | -:4", s::box, 0.4);
        return s.mix();
    }

    /** 吐槽模式：一蹦一跳的斷奏低音、反拍的撥弦和木魚。 */
    private static float[] comedy() {
        Score s = new Score(128, 64);
        String[] chords = {"G", "Em", "C", "D", "G", "Em", "C", "D", "G", "Em", "C", "D", "C", "D", "G", "G"};
        for (int bar = 0; bar < chords.length; bar++) {
            double at = bar * 4;
            int root = s.root(chords[bar]);
            for (int beat = 0; beat < 4; beat++) {
                s.bass(at + beat, 0.3, beat % 2 == 0 ? root : root + 7, 0.65);
                for (int n : s.voicing(chords[bar])) {
                    s.pluck(at + beat + 0.5, 0.25, n, 0.28);
                }
            }
            s.click(at + 1, 0.5);
            s.click(at + 3, 0.5);
            if (bar % 4 == 3) {
                s.click(at + 3.5, 0.35);
                s.click(at + 3.75, 0.35);
            }
        }
        String hook = "G5:0.5 -:0.5 D5:0.5 -:0.5 B4:0.5 D5:0.5 G5:1 | E5:0.5 -:0.5 G5:0.5 -:0.5 B5:1 A5:1"
                + " | G5:0.5 E5:0.5 C5:0.5 E5:0.5 G5:1 -:1 | F#5:0.5 -:0.5 A5:0.5 -:0.5 D6:1 -:1";
        s.melody(0, hook + " | " + hook + " | -:16 | E5:1 G5:1 C6:1 B5:1 | A5:1 F#5:1 D5:2"
                + " | G5:0.5 B5:0.5 D6:0.5 B5:0.5 G5:1 D5:1 | G5:2 -:2", s::pluck, 0.6);
        return s.mix();
    }

    /** 心跳超出預期值：音樂盒琶音和柔和鋪底，底下有心跳似的兩下低音。 */
    private static float[] heart() {
        Score s = new Score(72, 48);
        String[] chords = {"D", "Bm", "G", "A", "D", "Bm", "G", "A", "Em", "A", "D", "D"};
        int[] pattern = {0, 1, 2, 3, 4, 3, 2, 1};
        for (int bar = 0; bar < chords.length; bar++) {
            double at = bar * 4;
            int[] v = s.voicing(chords[bar]);
            int[] tones = {v[0] + 12, v[1] + 12, v[2] + 12, v[0] + 24, v[1] + 24};
            for (int i = 0; i < 8; i++) {
                s.box(at + i * 0.5, 0.5, tones[pattern[i]], 0.35);
            }
            for (int n : v) {
                s.pad(at, 4, n, 0.3);
            }
            s.bass(at, 3.5, s.root(chords[bar]), 0.35);
            s.thump(at, 0.5);
            s.thump(at + 0.4, 0.35);
        }
        s.melody(0, "F#5:2 E5:2 | D5:4 | B4:2 D5:2 | E5:4 | F#5:2 A5:2 | F#5:4 | G5:2 F#5:1 E5:1 | E5:4"
                + " | G5:2 F#5:2 | E5:2 C#5:2 | D5:4 | -:4", s::ep, 0.45);
        return s.mix();
    }

    /** 窗外的雨：長延音的電鋼琴往上散開，加一層鋪底。 */
    private static float[] rain() {
        Score s = new Score(66, 48);
        String[] chords = {"Am", "F", "C", "G", "Am", "F", "Dm", "E", "F", "G", "Am", "Am"};
        for (int bar = 0; bar < chords.length; bar++) {
            double at = bar * 4;
            int[] v = s.voicing(chords[bar]);
            for (int i = 0; i < v.length; i++) {
                s.ep(at + i * 0.5, 3.5 - i * 0.5, v[i], 0.35);
            }
            for (int n : v) {
                s.pad(at, 4, n - 12, 0.3);
            }
            s.bass(at, 3.8, s.root(chords[bar]), 0.4);
        }
        s.melody(0, "E5:2 D5:1 C5:1 | C5:3 -:1 | E5:2 G5:2 | D5:4 | E5:2 D5:1 C5:1 | A4:3 -:1"
                + " | F5:2 E5:1 D5:1 | B4:4 | A4:2 C5:2 | E5:4 | D5:2 C5:1 B4:1 | A4:4", s::ep, 0.5);
        return s.mix();
    }

    /** 一首曲子的樂譜和合成緩衝；時間單位是拍，超出結尾的音繞回開頭，循環才接得起來。 */
    static final class Score {

        interface Instrument {
            void note(double beat, double beats, int midi, double velocity);
        }

        private static final String[] NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

        private final double secondsPerBeat;
        private final float[] buf;
        private final Random rnd = new Random(11);

        Score(double bpm, int beats) {
            secondsPerBeat = 60 / bpm;
            buf = Synth.buffer(beats * secondsPerBeat);
        }

        private double sec(double beat) {
            return beat * secondsPerBeat;
        }

        float[] mix() {
            return Synth.normalize(buf, 0.85);
        }

        /** 「C#5」「Bb4」換成 MIDI 音高。 */
        static int midi(String name) {
            String letter = name.substring(0, name.length() - 1);
            int octave = Integer.parseInt(name.substring(name.length() - 1));
            return (octave + 1) * 12 + pitchClass(letter);
        }

        private static int pitchClass(String letter) {
            int pc = -1;
            String base = letter.substring(0, 1);
            for (int i = 0; i < NAMES.length; i++) {
                if (NAMES[i].equals(base)) {
                    pc = i;
                }
            }
            if (pc < 0) {
                throw new IllegalArgumentException("音名寫錯：" + letter);
            }
            if (letter.endsWith("#")) {
                pc++;
            } else if (letter.length() > 1 && letter.endsWith("b")) {
                pc--;
            }
            return Math.floorMod(pc, 12);
        }

        /** 和弦的根音，放在 E2 到 D#3 之間給低音用。 */
        int root(String chord) {
            int pc = pitchClass(chord.endsWith("m") ? chord.substring(0, chord.length() - 1) : chord);
            return 40 + Math.floorMod(pc - 4, 12);
        }

        /** 三和弦的密集排列，每個音都放在 A3 到 G#4 之間，換和弦時手不用跳太遠。 */
        int[] voicing(String chord) {
            boolean minor = chord.endsWith("m");
            int pc = pitchClass(minor ? chord.substring(0, chord.length() - 1) : chord);
            int[] pcs = {pc, pc + (minor ? 3 : 4), pc + 7};
            int[] out = new int[3];
            for (int i = 0; i < 3; i++) {
                out[i] = 57 + Math.floorMod(pcs[i] - 9, 12);
            }
            Arrays.sort(out);
            return out;
        }

        /** 「E5:1 -:1 G5:2」：音名加拍數，{@code -} 是休止，{@code |} 只是小節線。 */
        void melody(double startBeat, String seq, Instrument inst, double velocity) {
            double at = startBeat;
            for (String token : seq.trim().split("\\s+")) {
                if (token.equals("|")) {
                    continue;
                }
                String[] parts = token.split(":");
                double beats = Double.parseDouble(parts[1]);
                if (!parts[0].equals("-")) {
                    inst.note(at, beats, midi(parts[0]), velocity);
                }
                at += beats;
            }
        }

        /** 電鋼琴：簡單的 FM，敲下去亮、慢慢變圓。 */
        void ep(double beat, double beats, int midi, double vel) {
            double f = hz(midi);
            double dur = sec(beats);
            Synth.wrap(buf, sec(beat), dur + 0.7, t -> {
                double index = 1.4 * decay(t, 0.25) + 0.35;
                double s = Math.sin(2 * Math.PI * f * t + index * sine(t, f)) + 0.15 * sine(t, f * 2);
                double env = Math.min(1, t / 0.004) * decay(t, 1.1) * (t < dur ? 1 : Math.max(0, 1 - (t - dur) / 0.7));
                return s * env * vel * 0.25;
            });
        }

        /** 撥弦：Karplus-Strong，一段雜訊在延遲線裡來回衰減成弦聲。 */
        void pluck(double beat, double beats, int midi, double vel) {
            double f = hz(midi);
            int period = Math.max(2, (int) Math.round(Synth.RATE / f));
            double[] line = new double[period];
            for (int i = 0; i < period; i++) {
                line[i] = rnd.nextDouble() * 2 - 1;
            }
            double dur = Math.min(sec(beats) + 0.35, 1.6);
            int[] i = {0};
            Synth.wrap(buf, sec(beat), dur, t -> {
                int k = i[0]++ % period;
                double out = line[k];
                line[k] = 0.5 * (line[k] + line[(k + 1) % period]) * 0.996;
                double fadeOut = t > dur - 0.05 ? (dur - t) / 0.05 : 1;
                return out * vel * 0.3 * fadeOut;
            });
        }

        void bass(double beat, double beats, int midi, double vel) {
            double f = hz(midi);
            double dur = sec(beats);
            Synth.wrap(buf, sec(beat), dur + 0.1, t -> (sine(t, f) * 0.8 + 0.25 * sine(t, f * 2) + 0.1 * sine(t, f * 3))
                    * envelope(t, 0.01, dur, 0.08) * vel * 0.35);
        }

        /** 音樂盒：基音加上很快消失的高泛音。 */
        void box(double beat, double beats, int midi, double vel) {
            double f = hz(midi);
            Synth.wrap(buf, sec(beat), 1.6, t -> (sine(t, f) * decay(t, 0.9)
                    + 0.3 * sine(t, f * 3) * decay(t, 0.25)
                    + 0.12 * sine(t, f * 5.4) * decay(t, 0.08)) * Math.min(1, t / 0.002) * vel * 0.22);
        }

        /** 柔和鋪底：三個稍微走音的鋸齒波疊在一起再濾掉高頻，慢慢浮上來。 */
        void pad(double beat, double beats, int midi, double vel) {
            double f = hz(midi);
            double dur = sec(beats);
            Synth.LowPass lp = new Synth.LowPass(1400);
            Synth.wrap(buf, sec(beat), dur + 0.8, t -> {
                double saw = 0;
                for (double detune : new double[] {0.996, 1.0, 1.004}) {
                    double x = t * f * detune;
                    saw += 2 * (x - Math.floor(x)) - 1;
                }
                return lp.next(saw / 3) * envelope(t, 0.5, dur, 0.8) * vel * 0.12;
            });
        }

        void shaker(double beat, double vel) {
            double[] prev = {0};
            Synth.wrap(buf, sec(beat), 0.06, t -> {
                double n = rnd.nextDouble() * 2 - 1;
                double hp = n - prev[0];
                prev[0] = n;
                return hp * decay(t, 0.015) * vel * 0.08;
            });
        }

        void click(double beat, double vel) {
            Synth.wrap(buf, sec(beat), 0.08, t -> (sine(t, 1250) * decay(t, 0.02) + 0.3 * sine(t, 2600) * decay(t, 0.008)) * vel * 0.3);
        }

        void thump(double beat, double vel) {
            double[] phase = {0};
            Synth.wrap(buf, sec(beat), 0.35, t -> {
                double f = 70 - 25 * Math.min(1, t / 0.08);
                phase[0] += 2 * Math.PI * f / Synth.RATE;
                return Math.sin(phase[0]) * decay(t, 0.1) * vel * 0.5;
            });
        }
    }
}
