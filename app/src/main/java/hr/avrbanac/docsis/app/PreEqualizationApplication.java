package hr.avrbanac.docsis.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PreEqualizationApplication extends Application {

    @Override
    public void start(final Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(PreEqualizationApplication.class.getResource("/views/preeq-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
        stage.setTitle("PreEqualization");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
