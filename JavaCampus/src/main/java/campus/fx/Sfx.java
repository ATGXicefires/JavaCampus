package campus.fx;

import javax.sound.sampled.Clip;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;

import static campus.fx.Synth.add;
import static campus.fx.Synth.buffer;
import static campus.fx.Synth.decay;
import static campus.fx.Synth.envelope;
import static campus.fx.Synth.sine;

/** 全部用程式合成的音效、角色嗶聲和環境音。找不到音訊裝置時整組靜音，遊戲照常進行。 */
public final class Sfx implements AutoCloseable {

    public enum Sound {
        KEY, RUN, DING, BONK, GLITCH, HUH, FANFARE, UNLOCK, PAPER("翻頁"),
        CHOICE_IN, CHOICE_OK, AP, TP, TITLE, SCRIBBLE,
        BELL("鐘聲"), NOTIFY("手機通知"), DIAL("撥號"), CONNECT("接通"), DOOR_OPEN("開門"), DOOR_CLOSE("關門"),
        OOH("全班噢"), SLAP("吐槽"), HEARTBEAT("心跳");

        /** 劇本 {@code @音效} 用的名稱；只在程式裡用的音效是 null。 */
        public final String scriptName;

        Sound() {
            this(null);
        }

        Sound(String scriptName) {
            this.scriptName = scriptName;
        }

        public static Optional<Sound> byScriptName(String name) {
            for (Sound s : values()) {
                if (name.equals(s.scriptName)) {
                    return Optional.of(s);
                }
            }
            return Optional.empty();
        }
    }

    public enum Ambience {
        RAIN("雨"), ROOM("電腦教室"), CICADA("蟬鳴"), HALLWAY("走廊人聲");

        public final String scriptName;

        Ambience(String scriptName) {
            this.scriptName = scriptName;
        }

        public static Optional<Ambience> byScriptName(String name) {
            for (Ambience a : values()) {
                if (a.scriptName.equals(name)) {
                    return Optional.of(a);
                }
            }
            return Optional.empty();
        }
    }

    /**
     * 角色說話時的嗶聲。
     *
     * @param every 逐字出現時每幾個字響一聲
     */
    public enum Voice {
        ERHONG("二弘", 3), TINGXUAN("亭瑄", 2), ACHENG("阿成", 2), XIAOYUN("小芸", 3),
        TEACHER("王老師", 3), GRANDMA("阿嬤", 3), OTHER(null, 3);

        public final String person;
        public final int every;

        Voice(String person, int every) {
            this.person = person;
            this.every = every;
        }

        /** 名冊裡沒有的角色（全班、同學）用中性的聲音。 */
        public static Voice of(String personId) {
            for (Voice v : values()) {
                if (v.person != null && v.person.equals(personId)) {
                    return v;
                }
            }
            return OTHER;
        }
    }

    private static final double AMBIENT_LEVEL = 0.35;
    private static final double AMBIENT_FADE = 1.2;

    private final Map<Sound, Clip[]> clips = new EnumMap<>(Sound.class);
    private final Map<Sound, Integer> next = new EnumMap<>(Sound.class);
    private final Map<Voice, Clip[]> voices = new EnumMap<>(Voice.class);
    private final Map<Ambience, Clip> ambience = new EnumMap<>(Ambience.class);
    private final Fader fader;
    private final Random pick = new Random();
    private Collection<Ambience> ambientOn = List.of();
    private double effects = 0.7;
    private double voice = 0.4;
    private double ambient = 0.2;
    private boolean typing = true;

    private Sfx(Fader fader) {
        this.fader = fader;
    }

    public static Sfx create() {
        Sfx sfx = new Sfx(new Fader());
        try {
            Random rnd = new Random(7);
            for (Sound s : Sound.values()) {
                int pool = s == Sound.KEY ? 6 : s == Sound.SCRIBBLE ? 4 : 2;
                Clip[] arr = new Clip[pool];
                for (int i = 0; i < pool; i++) {
                    arr[i] = Synth.open(synth(s, rnd));
                }
                sfx.clips.put(s, arr);
                sfx.next.put(s, 0);
            }
            for (Voice v : Voice.values()) {
                Clip[] arr = new Clip[5];
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = Synth.open(blip(v, rnd));
                }
                sfx.voices.put(v, arr);
            }
            for (Ambience a : Ambience.values()) {
                sfx.ambience.put(a, Synth.open(loop(a, rnd)));
            }
        } catch (Exception | LinkageError e) {
            System.err.println("音效停用：" + e.getMessage());
            sfx.close();
            sfx.clips.clear();
            sfx.voices.clear();
            sfx.ambience.clear();
        }
        return sfx;
    }

    /** 背景音樂和環境音共用同一條淡入淡出的執行緒。 */
    Fader fader() {
        return fader;
    }

    public void play(Sound sound) {
        if (sound == Sound.KEY && !typing) {
            return;
        }
        Clip[] pool = clips.get(sound);
        if (pool == null) {
            return;
        }
        int i = next.get(sound);
        next.put(sound, (i + 1) % pool.length);
        start(pool[i], effects);
    }

    public void blip(Voice v) {
        Clip[] pool = voices.get(v);
        if (pool == null || voice <= 0) {
            return;
        }
        start(pool[v == Voice.XIAOYUN ? 0 : pick.nextInt(pool.length)], voice);
    }

    private static void start(Clip c, double level) {
        c.stop();
        c.setFramePosition(0);
        Synth.gain(c, level);
        c.start();
    }

    /** 換成這一組環境音：不在裡面的淡出，新加的淡入。 */
    public void setAmbience(Collection<Ambience> on) {
        ambientOn = List.copyOf(on);
        applyAmbience(AMBIENT_FADE);
    }

    private void applyAmbience(double seconds) {
        for (Map.Entry<Ambience, Clip> e : ambience.entrySet()) {
            double target = ambientOn.contains(e.getKey()) ? ambient : 0;
            if (target > 0 || fader.isPlaying(e.getValue())) {
                fader.fadeTo(e.getValue(), target, seconds);
            }
        }
    }

    /** 參數都是 0 到 1。 */
    public void setVolumes(double master, double sfx, double ambientVolume, double voiceVolume) {
        effects = master * sfx;
        voice = master * voiceVolume;
        ambient = master * ambientVolume * AMBIENT_LEVEL;
        applyAmbience(0.05);
    }

    public void setTyping(boolean on) {
        typing = on;
    }

    @Override
    public void close() {
        fader.close();
        clips.values().forEach(Sfx::closeAll);
        voices.values().forEach(Sfx::closeAll);
        ambience.values().forEach(Clip::close);
    }

    private static void closeAll(Clip[] arr) {
        for (Clip c : arr) {
            if (c != null) {
                c.close();
            }
        }
    }

    private static float[] synth(Sound s, Random rnd) {
        return switch (s) {
            case KEY -> key(rnd);
            case RUN -> run(rnd);
            case DING -> ding();
            case BONK -> bonk(rnd);
            case GLITCH -> glitch(rnd, 0.35);
            case HUH -> huh();
            case FANFARE -> fanfare();
            case UNLOCK -> sweep();
            case PAPER -> paper(rnd);
            case CHOICE_IN -> choiceIn();
            case CHOICE_OK -> choiceOk(rnd);
            case AP -> apBell();
            case TP -> tpWarm(rnd);
            case TITLE -> title(rnd);
            case SCRIBBLE -> scribble(rnd);
            case BELL -> westminster();
            case NOTIFY -> notify2();
            case DIAL -> dial();
            case CONNECT -> connect(rnd);
            case DOOR_OPEN -> door(rnd, false);
            case DOOR_CLOSE -> door(rnd, true);
            case OOH -> ooh(rnd);
            case SLAP -> slap(rnd);
            case HEARTBEAT -> heartbeat();
        };
    }

    private static double noise(Random rnd) {
        return rnd.nextDouble() * 2 - 1;
    }

    private static float[] key(Random rnd) {
        float[] b = buffer(0.06);
        double pitch = 0.9 + rnd.nextDouble() * 0.2;
        add(b, 0, 0.06, t -> noise(rnd) * 0.5 * decay(t, 0.004));
        add(b, 0, 0.06, t -> sine(t, 1800 * pitch) * 0.25 * decay(t, 0.008));
        add(b, 0, 0.06, t -> sine(t, 170 * pitch) * 0.35 * decay(t, 0.015));
        return b;
    }

    private static float[] run(Random rnd) {
        float[] b = buffer(0.2);
        for (double at : new double[] {0, 0.075}) {
            add(b, at, 0.08, t -> noise(rnd) * 0.45 * decay(t, 0.003));
            add(b, at, 0.08, t -> sine(t, 900) * 0.3 * decay(t, 0.012));
            add(b, at, 0.08, t -> sine(t, 140) * 0.3 * decay(t, 0.02));
        }
        return b;
    }

    /** 程式跑起來的「叮——」：清亮但短，不搶劇情的戲。 */
    private static float[] ding() {
        float[] b = buffer(0.8);
        double f = 1318.5;
        add(b, 0, 0.8, t -> {
            double env = Math.min(1, t / 0.003) * decay(t, 0.3);
            return (sine(t, f) + 0.25 * sine(t, f * 2) + 0.08 * sine(t, f * 3)) * 0.3 * env;
        });
        add(b, 0, 0.2, t -> sine(t, f * 2.01) * 0.08 * decay(t, 0.04));
        return b;
    }

    /** 編譯錯誤的「咚」：像敲木魚，喜劇感，不刺耳。 */
    private static float[] bonk(Random rnd) {
        float[] b = buffer(0.35);
        double[] phase = {0};
        add(b, 0, 0.3, t -> {
            double hz = 190 - 50 * Math.min(1, t / 0.08);
            phase[0] += 2 * Math.PI * hz / Synth.RATE;
            return Math.sin(phase[0]) * 0.6 * decay(t, 0.07);
        });
        add(b, 0, 0.12, t -> sine(t, 437) * 0.2 * decay(t, 0.025));
        add(b, 0, 0.01, t -> noise(rnd) * 0.3 * decay(t, 0.002));
        return b;
    }

    /** 執行到一半跌倒：音高亂跳、位元壓縮過的方波。 */
    private static float[] glitch(Random rnd, double seconds) {
        float[] b = buffer(seconds);
        double[] hz = {600};
        int[] count = {0};
        add(b, 0, seconds, t -> {
            if (count[0]++ % 1300 == 0) {
                hz[0] = 180 + rnd.nextDouble() * 1100;
            }
            double sq = Math.signum(sine(t, hz[0]));
            double crushed = Math.round((sq * 0.7 + noise(rnd) * 0.3) * 3) / 3.0;
            return crushed * 0.16 * envelope(t, 0.005, seconds - 0.06, 0.06);
        });
        return b;
    }

    /** 能跑但答案不對：疑問感的兩個音往下走。 */
    private static float[] huh() {
        float[] b = buffer(0.5);
        double[][] notes = {{0, 440}, {0.16, 349.2}};
        for (double[] n : notes) {
            add(b, n[0], 0.3, t -> (sine(t, n[1]) + 0.15 * sine(t, n[1] * 2)) * 0.22 * Math.min(1, t / 0.02) * decay(t, 0.12));
        }
        return b;
    }

    private static float[] fanfare() {
        float[] b = buffer(1.4);
        double[] notes = {523.25, 659.25, 783.99, 1046.5};
        for (int i = 0; i < notes.length; i++) {
            double f = notes[i];
            double len = i == notes.length - 1 ? 1.0 : 0.35;
            add(b, i * 0.09, len, t -> {
                double mod = (1.0 * decay(t, 0.2) + 0.2) * sine(t, f);
                return Math.sin(2 * Math.PI * f * t + mod) * 0.18 * Math.min(1, t / 0.004) * decay(t, len * 0.45);
            });
        }
        return b;
    }

    private static float[] sweep() {
        float[] b = buffer(0.45);
        double duration = 0.32;
        double[] phase = {0};
        add(b, 0, duration, t -> {
            double hz = 400 + (1200 - 400) * (t / duration);
            phase[0] += 2 * Math.PI * hz / Synth.RATE;
            return Math.sin(phase[0]) * 0.22 * Math.sin(Math.PI * t / duration);
        });
        add(b, 0.28, 0.17, t -> sine(t, 2400) * 0.12 * decay(t, 0.04));
        return b;
    }

    private static float[] paper(Random rnd) {
        float[] b = buffer(0.3);
        double[] lp = {0};
        double[] prev = {0};
        add(b, 0, 0.28, t -> {
            lp[0] += 0.3 * (noise(rnd) - lp[0]);
            double band = lp[0] - prev[0] * 0.6;
            prev[0] = lp[0];
            return band * 0.5 * Math.sin(Math.PI * t / 0.28);
        });
        return b;
    }

    private static float[] choiceIn() {
        float[] b = buffer(0.35);
        double[][] notes = {{0, 659.25}, {0.06, 880}};
        for (double[] n : notes) {
            add(b, n[0], 0.25, t -> sine(t, n[1]) * 0.16 * Math.min(1, t / 0.005) * decay(t, 0.08));
        }
        return b;
    }

    /** 點選選項的「啵」。 */
    private static float[] choiceOk(Random rnd) {
        float[] b = buffer(0.18);
        double[] phase = {0};
        add(b, 0, 0.15, t -> {
            double hz = 900 - 600 * Math.min(1, t / 0.06);
            phase[0] += 2 * Math.PI * hz / Synth.RATE;
            return Math.sin(phase[0]) * 0.35 * decay(t, 0.035);
        });
        add(b, 0, 0.01, t -> noise(rnd) * 0.15 * decay(t, 0.002));
        return b;
    }

    private static float[] apBell() {
        float[] b = buffer(0.8);
        double f = 1046.5;
        add(b, 0, 0.8, t -> (sine(t, f) + 0.4 * sine(t, f * 2.0) + 0.2 * sine(t, f * 3.01))
                * 0.18 * Math.min(1, t / 0.003) * decay(t, 0.28));
        return b;
    }

    /** 信任：低沉溫暖的兩個音，尾巴一聲小鎖頭打開的「喀」。 */
    private static float[] tpWarm(Random rnd) {
        float[] b = buffer(0.9);
        add(b, 0, 0.8, t -> (sine(t, 196) + 0.6 * sine(t, 392) + 0.15 * sine(t, 588))
                * 0.22 * Math.min(1, t / 0.04) * decay(t, 0.3));
        Synth.BandPass bp = new Synth.BandPass(3200, 3);
        add(b, 0.34, 0.04, t -> bp.next(noise(rnd)) * 1.2 * decay(t, 0.006));
        return b;
    }

    /** 章節標題卡：先閃一下故障，再一聲柔和的叮。 */
    private static float[] title(Random rnd) {
        float[] b = buffer(1.3);
        float[] g = glitch(rnd, 0.22);
        System.arraycopy(g, 0, b, 0, g.length);
        add(b, 0.32, 0.95, t -> (sine(t, 1046.5) + 0.5 * sine(t, 1568)) * 0.16 * Math.min(1, t / 0.01) * decay(t, 0.35));
        return b;
    }

    private static float[] scribble(Random rnd) {
        float[] b = buffer(0.07);
        Synth.BandPass bp = new Synth.BandPass(2500 + rnd.nextDouble() * 1500, 1.5);
        double rate = 25 + rnd.nextDouble() * 20;
        add(b, 0, 0.07, t -> bp.next(noise(rnd)) * (0.5 + 0.5 * Math.sin(2 * Math.PI * rate * t)) * 0.5
                * Math.sin(Math.PI * t / 0.07));
        return b;
    }

    /** 學校鐘聲：西敏寺鐘聲的四句旋律（公有領域），鐘的泛音不是整數倍。 */
    private static float[] westminster() {
        double[] e = {329.63, 369.99, 415.30, 246.94};
        int[][] phrases = {{2, 1, 0, 3}, {0, 2, 1, 0}, {2, 0, 1, 3}, {3, 1, 2, 0}};
        double beat = 0.5;
        float[] b = buffer(4 * 4 * beat + 3 * beat + 3.2);
        double at = 0;
        for (int[] phrase : phrases) {
            for (int i = 0; i < phrase.length; i++) {
                double f = e[phrase[i]];
                double len = i == phrase.length - 1 ? 2.6 : 1.6;
                add(b, at, len, t -> {
                    double env = Math.min(1, t / 0.004);
                    return (0.5 * sine(t, f * 0.5) * decay(t, 1.2)
                            + sine(t, f) * decay(t, 0.9)
                            + 0.45 * sine(t, f * 1.19) * decay(t, 0.5)
                            + 0.35 * sine(t, f * 1.5) * decay(t, 0.4)
                            + 0.25 * sine(t, f * 2.0) * decay(t, 0.3)
                            + 0.15 * sine(t, f * 2.52) * decay(t, 0.2)) * 0.11 * env;
                });
                at += beat;
            }
            at += beat;
        }
        return b;
    }

    /** 原創的兩音通知聲，刻意不像任何通訊軟體。 */
    private static float[] notify2() {
        float[] b = buffer(0.4);
        double[][] notes = {{0, 880}, {0.08, 1318.5}};
        for (double[] n : notes) {
            add(b, n[0], 0.3, t -> (sine(t, n[1]) + 0.2 * sine(t, n[1] * 3)) * 0.2 * Math.min(1, t / 0.003) * decay(t, 0.09));
        }
        return b;
    }

    private static float[] dial() {
        float[] b = buffer(1.25);
        for (double at : new double[] {0, 0.6}) {
            add(b, at, 0.42, t -> sine(t, 425) * 0.12 * envelope(t, 0.01, 0.4, 0.02));
        }
        return b;
    }

    private static float[] connect(Random rnd) {
        float[] b = buffer(0.2);
        double[] phase = {0};
        add(b, 0, 0.08, t -> {
            double hz = 600 + 300 * t / 0.08;
            phase[0] += 2 * Math.PI * hz / Synth.RATE;
            return Math.sin(phase[0]) * 0.2 * envelope(t, 0.005, 0.06, 0.02);
        });
        add(b, 0.09, 0.02, t -> noise(rnd) * 0.15 * decay(t, 0.003));
        return b;
    }

    private static float[] door(Random rnd, boolean close) {
        float[] b = buffer(0.6);
        double loud = close ? 0.8 : 0.45;
        double[] phase = {0};
        add(b, 0.03, 0.5, t -> {
            double hz = (close ? 75 : 95) - 25 * Math.min(1, t / 0.1);
            phase[0] += 2 * Math.PI * hz / Synth.RATE;
            return Math.sin(phase[0]) * loud * decay(t, close ? 0.16 : 0.1);
        });
        add(b, 0.03, 0.3, t -> sine(t, 223) * 0.18 * loud * decay(t, 0.06));
        add(b, 0, 0.02, t -> noise(rnd) * 0.25 * decay(t, 0.004));
        if (close) {
            Synth.LowPass lp = new Synth.LowPass(900);
            add(b, 0.04, 0.15, t -> lp.next(noise(rnd)) * 0.6 * decay(t, 0.05));
        }
        return b;
    }

    /** 全班男生齊聲「噢——」：幾個不同音高的鋸齒波，經過「o」的兩個共振峰，一起往上滑。 */
    private static float[] ooh(Random rnd) {
        double len = 1.5;
        float[] b = buffer(len);
        for (int v = 0; v < 9; v++) {
            double base = 105 + rnd.nextDouble() * 90;
            double vib = 4 + rnd.nextDouble() * 2;
            double delay = rnd.nextDouble() * 0.08;
            Synth.BandPass f1 = new Synth.BandPass(450, 4);
            Synth.BandPass f2 = new Synth.BandPass(820, 5);
            double[] phase = {rnd.nextDouble()};
            add(b, delay, len - delay, t -> {
                double hz = base * (1 + 0.35 * Math.min(1, t / 0.9)) * (1 + 0.012 * Math.sin(2 * Math.PI * vib * t));
                phase[0] += hz / Synth.RATE;
                double saw = 2 * (phase[0] - Math.floor(phase[0])) - 1;
                double voiced = f1.next(saw) * 1.0 + f2.next(saw) * 0.5;
                return voiced * 0.22 * envelope(t, 0.15, len - delay - 0.45, 0.4);
            });
        }
        return b;
    }

    /** 吐槽的「啪」。 */
    private static float[] slap(Random rnd) {
        float[] b = buffer(0.12);
        double[] prev = {0};
        add(b, 0, 0.06, t -> {
            double n = noise(rnd);
            double hp = n - prev[0] * 0.85;
            prev[0] = n;
            return hp * 0.6 * decay(t, 0.012);
        });
        add(b, 0, 0.03, t -> sine(t, 1800) * 0.15 * decay(t, 0.008));
        return b;
    }

    private static float[] heartbeat() {
        float[] b = buffer(0.75);
        double[][] beats = {{0, 0.9}, {0.27, 0.65}};
        for (double[] beat : beats) {
            double[] phase = {0};
            add(b, beat[0], 0.4, t -> {
                double hz = 70 - 25 * Math.min(1, t / 0.08);
                phase[0] += 2 * Math.PI * hz / Synth.RATE;
                return Math.sin(phase[0]) * beat[1] * decay(t, 0.09);
            });
        }
        return b;
    }

    private static float[] blip(Voice v, Random rnd) {
        double spread = switch (v) {
            case XIAOYUN -> 0;
            case ACHENG -> 0.08;
            default -> 0.03;
        };
        double p = 1 + (rnd.nextDouble() * 2 - 1) * spread;
        return switch (v) {
            case ERHONG -> tone(0.035, t -> {
                double f = 150 * p;
                return (sine(t, f) + 0.3 * sine(t, f * 3) + 0.15 * sine(t, f * 5)) * 0.35;
            });
            case TINGXUAN -> {
                double[] phase = {0};
                yield tone(0.045, t -> {
                    double f = 520 * p * (1 + 0.1 * Math.min(1, t / 0.04));
                    phase[0] += 2 * Math.PI * f / Synth.RATE;
                    return Math.sin(phase[0]) * 0.32;
                });
            }
            case ACHENG -> tone(0.04, t -> triangle(t, 330 * p) * 0.35);
            case XIAOYUN -> tone(0.04, t -> sine(t, 440) * 0.3);
            case TEACHER -> tone(0.05, t -> (sine(t, 260 * p) + 0.2 * sine(t, 520 * p)) * 0.35);
            case GRANDMA -> tone(0.055, t -> sine(t, 300 * p * (1 + 0.03 * Math.sin(2 * Math.PI * 8 * t))) * 0.35);
            case OTHER -> tone(0.04, t -> triangle(t, 380 * p) * 0.3);
        };
    }

    private static float[] tone(double seconds, DoubleUnaryOperator f) {
        float[] b = buffer(seconds + 0.01);
        add(b, 0, seconds, t -> f.applyAsDouble(t) * envelope(t, 0.003, seconds * 0.4, seconds * 0.6));
        return b;
    }

    private static double triangle(double t, double hz) {
        double x = t * hz - Math.floor(t * hz);
        return 4 * Math.abs(x - 0.5) - 1;
    }

    private static float[] loop(Ambience a, Random rnd) {
        return switch (a) {
            case RAIN -> rain(rnd);
            case ROOM -> room(rnd);
            case CICADA -> cicada(rnd);
            case HALLWAY -> hallway(rnd);
        };
    }

    /** 8 秒的雨聲循環：低通雜訊加隨機水滴。 */
    private static float[] rain(Random rnd) {
        double loop = 8.0;
        double fade = 0.5;
        float[] raw = buffer(loop + fade);
        double[] lp = {0};
        add(raw, 0, loop + fade, t -> {
            lp[0] += 0.08 * (noise(rnd) - lp[0]);
            return lp[0] * 0.9;
        });
        for (int i = 0; i < 90; i++) {
            double at = rnd.nextDouble() * (loop + fade - 0.05);
            double hz = 2000 + rnd.nextDouble() * 2500;
            double amp = 0.02 + rnd.nextDouble() * 0.05;
            add(raw, at, 0.05, t -> sine(t, hz) * amp * decay(t, 0.006));
        }
        return Synth.loopify(raw, loop, fade);
    }

    /** 電腦教室：冷氣的低頻嗡嗡聲加一排電腦風扇。 */
    private static float[] room(Random rnd) {
        double loop = 6.0;
        double fade = 0.5;
        float[] raw = buffer(loop + fade);
        Synth.LowPass fan = new Synth.LowPass(450);
        add(raw, 0, loop + fade, t -> sine(t, 60) * 0.12 + sine(t, 120) * 0.06 + fan.next(noise(rnd)) * 0.9);
        return Synth.normalize(Synth.loopify(raw, loop, fade), 0.5);
    }

    /** 九月的蟬：高頻帶通雜訊，用快速的脈衝調變出「唧唧」，幾隻蟬此起彼落。 */
    private static float[] cicada(Random rnd) {
        double loop = 10.0;
        double fade = 1.0;
        float[] raw = buffer(loop + fade);
        for (int c = 0; c < 4; c++) {
            Synth.BandPass bp = new Synth.BandPass(4200 + rnd.nextDouble() * 1800, 6);
            double pulse = 55 + rnd.nextDouble() * 30;
            double cycle = 2.5 + rnd.nextDouble() * 2;
            double offset = rnd.nextDouble() * cycle;
            double amp = 0.5 + rnd.nextDouble() * 0.5;
            add(raw, 0, loop + fade, t -> {
                double swell = Math.pow(Math.max(0, Math.sin(Math.PI * ((t + offset) % cycle) / cycle)), 1.5);
                double buzz = 0.5 + 0.5 * Math.signum(Math.sin(2 * Math.PI * pulse * t));
                return bp.next(noise(rnd)) * buzz * swell * amp;
            });
        }
        return Synth.normalize(Synth.loopify(raw, loop, fade), 0.5);
    }

    /** 下課的走廊：聽不出字的嘈雜人聲，用雜訊經過母音共振峰，再以說話的節奏起伏。 */
    private static float[] hallway(Random rnd) {
        double loop = 10.0;
        double fade = 1.0;
        float[] raw = buffer(loop + fade);
        double[][] vowels = {{700, 1200}, {400, 2000}, {500, 900}, {300, 2300}};
        for (int v = 0; v < 6; v++) {
            double[] formant = vowels[rnd.nextInt(vowels.length)];
            Synth.BandPass f1 = new Synth.BandPass(formant[0] * (0.9 + rnd.nextDouble() * 0.2), 5);
            Synth.BandPass f2 = new Synth.BandPass(formant[1] * (0.9 + rnd.nextDouble() * 0.2), 6);
            double syllable = 3.5 + rnd.nextDouble() * 2.5;
            double phrase = 1.5 + rnd.nextDouble() * 2;
            double offset = rnd.nextDouble() * 5;
            add(raw, 0, loop + fade, t -> {
                double talking = Math.max(0, Math.sin(2 * Math.PI * (t + offset) / (phrase * 2)));
                double syl = 0.5 + 0.5 * Math.sin(2 * Math.PI * syllable * t);
                double n = noise(rnd);
                return (f1.next(n) + 0.6 * f2.next(n)) * talking * syl;
            });
        }
        return Synth.normalize(Synth.loopify(raw, loop, fade), 0.5);
    }
}
