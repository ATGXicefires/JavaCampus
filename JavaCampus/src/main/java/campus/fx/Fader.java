package campus.fx;

import javax.sound.sampled.Clip;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** 在背景執行緒慢慢調整循環音軌的音量，換曲、換環境音時交叉淡化；降到 0 的音軌會停掉，不在背景空轉。 */
final class Fader implements AutoCloseable {

    private static final long TICK_MS = 20;

    private static final class Fade {
        double current;
        double target;
        double perSecond;
    }

    private final Map<Clip, Fade> fades = new HashMap<>();
    private final Thread thread;
    private volatile boolean running = true;

    Fader() {
        thread = new Thread(this::loop, "audio-fader");
        thread.setDaemon(true);
        thread.start();
    }

    /** 沒在播的音軌會從 0 開始循環播放，再淡入到 {@code target}。 */
    synchronized void fadeTo(Clip c, double target, double seconds) {
        Fade f = fades.get(c);
        if (f == null) {
            f = new Fade();
            f.current = c.isRunning() ? target : 0;
            fades.put(c, f);
        }
        if (!c.isRunning() && target > 0) {
            f.current = 0;
            Synth.gain(c, 0);
            c.loop(Clip.LOOP_CONTINUOUSLY);
        }
        f.target = target;
        f.perSecond = seconds <= 0 ? Double.MAX_VALUE : Math.max(Math.abs(target - f.current), 0.01) / seconds;
    }

    synchronized boolean isPlaying(Clip c) {
        Fade f = fades.get(c);
        return f != null && f.target > 0;
    }

    private void loop() {
        while (running) {
            synchronized (this) {
                Iterator<Map.Entry<Clip, Fade>> it = fades.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Clip, Fade> e = it.next();
                    Fade f = e.getValue();
                    double step = f.perSecond * TICK_MS / 1000.0;
                    if (Math.abs(f.target - f.current) <= step) {
                        f.current = f.target;
                    } else {
                        f.current += Math.signum(f.target - f.current) * step;
                    }
                    Synth.gain(e.getKey(), f.current);
                    if (f.current <= 0 && f.target <= 0) {
                        e.getKey().stop();
                        it.remove();
                    }
                }
            }
            try {
                Thread.sleep(TICK_MS);
            } catch (InterruptedException ex) {
                return;
            }
        }
    }

    @Override
    public void close() {
        running = false;
        thread.interrupt();
    }
}
