package campus.fx;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/** 不需要音訊裝置的 Clip：只記下有沒有在播、從哪裡播和目前音量，給淡入淡出的測試用。 */
final class FakeClip {

    final Clip clip;
    final FloatControl gain = new FloatControl(FloatControl.Type.MASTER_GAIN, -80, 6, 0.01f, -1, 0, "dB") {};
    boolean running;
    int starts;
    int frame;

    FakeClip() {
        clip = (Clip) Proxy.newProxyInstance(FakeClip.class.getClassLoader(), new Class<?>[] {Clip.class}, this::invoke);
    }

    /** 在播而且音量高於最小值，喇叭才真的有聲音。 */
    boolean audible() {
        return running && gain.getValue() > gain.getMinimum();
    }

    private Object invoke(Object proxy, Method m, Object[] args) {
        return switch (m.getName()) {
            case "loop", "start" -> {
                running = true;
                starts++;
                yield null;
            }
            case "stop" -> {
                running = false;
                yield null;
            }
            case "setFramePosition" -> {
                frame = (int) args[0];
                yield null;
            }
            case "isRunning", "isActive" -> running;
            case "isControlSupported" -> args[0] == FloatControl.Type.MASTER_GAIN;
            case "getControl" -> gain;
            case "close" -> null;
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            case "toString" -> "FakeClip@" + System.identityHashCode(proxy);
            default -> throw new UnsupportedOperationException(m.getName());
        };
    }
}
