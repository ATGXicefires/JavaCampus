package detective;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        CodeArea area = new CodeArea("System.out.println(\"報到\");");
        area.setParagraphGraphicFactory(LineNumberFactory.get(area));
        stage.setScene(new Scene(area, 800, 500));
        stage.setTitle("Java 偵探社");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
