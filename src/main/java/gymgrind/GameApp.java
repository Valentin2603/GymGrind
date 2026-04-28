package gymgrind;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class GameApp extends Application {

    @Override
    public void start(Stage stage) {
        GameController controller = new GameController(stage);
        Scene scene = controller.createScene();

        stage.setTitle("Путь к сцене: симулятор качка");
        stage.setMinWidth(1100);
        stage.setMinHeight(720);
        stage.setScene(scene);
        stage.show();

        controller.start();
    }
}
