package detective.ui;

import detective.progress.Progress;
import javafx.geometry.Insets;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

final class SoundMenu {

    private SoundMenu() {}

    static MenuButton create(Game game) {
        Progress.Settings s = game.progress.settings();
        CheckMenuItem rain = new CheckMenuItem("雨聲");
        rain.setSelected(s.ambient());
        rain.setOnAction(e -> {
            s.setAmbient(rain.isSelected());
            game.sfx.setAmbient(rain.isSelected());
            game.save();
        });
        CheckMenuItem typing = new CheckMenuItem("打字音效");
        typing.setSelected(s.typing());
        typing.setOnAction(e -> {
            s.setTyping(typing.isSelected());
            game.sfx.setTyping(typing.isSelected());
            game.save();
        });
        Slider volume = new Slider(0, 1, s.volume());
        volume.setPrefWidth(160);
        volume.valueChangingProperty().addListener((obs, was, changing) -> {
            if (!changing) {
                game.save();
            }
        });
        volume.valueProperty().addListener((obs, old, v) -> {
            s.setVolume(v.doubleValue());
            game.sfx.setVolume(v.doubleValue());
        });
        Label volumeLabel = new Label("音量");
        volumeLabel.getStyleClass().add("muted");
        VBox box = new VBox(4, volumeLabel, volume);
        box.setPadding(new Insets(4, 8, 4, 8));
        CustomMenuItem volumeItem = new CustomMenuItem(box, false);
        MenuButton menu = new MenuButton("音效", null, rain, typing, volumeItem);
        menu.getStyleClass().add("btn-ghost");
        return menu;
    }
}
