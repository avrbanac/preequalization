package hr.avrbanac.docsis.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Objects;

import static hr.avrbanac.docsis.app.PreEqAppConfiguration.*;

public class PreEqualizationApplication extends Application {

    @Override
    public void start(final Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(PreEqualizationApplication.class.getResource(APP_VIEW_FILE));
        Scene scene = new Scene(fxmlLoader.load(), APP_WIDTH, APP_HEIGHT);
        scene.getStylesheets().add(APP_CSS_FILE);
        stage.getIcons().addAll(
                new Image(Objects.requireNonNull(PreEqualizationApplication.class.getResourceAsStream(ICON_16_FILE))),
                new Image(Objects.requireNonNull(PreEqualizationApplication.class.getResourceAsStream(ICON_32_FILE))),
                new Image(Objects.requireNonNull(PreEqualizationApplication.class.getResourceAsStream(ICON_64_FILE))));
        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
