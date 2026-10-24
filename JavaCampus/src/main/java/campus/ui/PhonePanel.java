package campus.ui;

import campus.fx.Motion;
import campus.fx.Sfx;
import campus.story.Cast;
import campus.story.Route;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 劇情裡的手機聊天。原創的介面，不模仿任何通訊軟體；視角角色自己的訊息靠右。 */
final class PhonePanel extends StackPane {

    private final Game game;
    private final Route route;
    private final Label contact = new Label();
    private final VBox messages = new VBox(10);
    private final ScrollPane scroll = new ScrollPane(messages);
    private final Map<String, List<Node>> threads = new HashMap<>();
    private String current;
    private HBox unsent;

    PhonePanel(Game game, Route route) {
        this.game = game;
        this.route = route;
        contact.getStyleClass().add("phone-contact");
        HBox header = new HBox(contact);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("phone-header");
        messages.setPadding(new Insets(14));
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("phone-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        VBox frame = new VBox(header, scroll);
        frame.getStyleClass().add("phone");
        frame.setPrefSize(400, 640);
        frame.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        getChildren().add(frame);
        StackPane.setAlignment(frame, Pos.CENTER_RIGHT);
        setPadding(new Insets(0, 120, 60, 0));
        setPickOnBounds(false);
    }

    /** 每個聯絡人各留一串對話，切去跟別人聊再切回來，之前的訊息和附件都還在。 */
    void open(String who) {
        if (!who.equals(current)) {
            if (current != null) {
                threads.put(current, new ArrayList<>(messages.getChildren()));
            }
            current = who;
            contact.setText(who);
            messages.getChildren().setAll(threads.getOrDefault(who, List.of()));
            unsent = null;
        }
    }

    /** 對方傳來的訊息先出現「⋯」一下，再換成內容；{@code onShown} 在訊息完整出現後呼叫。 */
    void message(String who, String text, boolean mine, boolean instant, Runnable onShown) {
        if (unsent != null) {
            unsent.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            unsent = null;
        }
        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(270);
        bubble.getStyleClass().addAll("bubble", mine ? "mine" : "theirs");
        if (mine) {
            bubble.setStyle("-fx-background-color: " + Cast.colorOf(route.self) + ";");
        }
        if (mine || instant) {
            add(bubble, mine);
            if (!mine) {
                game.sfx.play(Sfx.Sound.NOTIFY);
            }
            onShown.run();
            return;
        }
        Label dots = new Label("⋯");
        dots.getStyleClass().addAll("bubble", "theirs", "typing");
        Node row = add(dots, false);
        PauseTransition wait = new PauseTransition(Duration.millis(520));
        wait.setOnFinished(e -> {
            messages.getChildren().remove(row);
            add(bubble, false);
            game.sfx.play(Sfx.Sound.NOTIFY);
            onShown.run();
        });
        wait.play();
    }

    /** 附件算是緊接在後面那則訊息的人傳的，等那則訊息出現才決定靠左還是靠右。 */
    void attachment(String fileName, boolean instant, Runnable open) {
        Button file = new Button("📄  " + fileName);
        file.getStyleClass().add("attachment");
        file.setOnAction(e -> open.run());
        unsent = (HBox) add(file, false);
        if (!instant) {
            game.sfx.play(Sfx.Sound.NOTIFY);
        }
    }

    private Node add(Node bubble, boolean mine) {
        HBox row = new HBox(bubble);
        row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messages.getChildren().add(row);
        Motion.rise(row, Duration.ZERO);
        scroll.layout();
        scroll.setVvalue(1);
        return row;
    }
}
