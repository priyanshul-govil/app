package secur3dit;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author naman
 */
public class Main extends Application {
    public static Stage stage = null;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("ui/homepage.fxml"));
        Scene s = new Scene(root);
        s.getStylesheets().add(getClass().getResource("ui/design.css").toExternalForm());
        stage.setScene(s);
        this.stage = stage;
        stage.show();

    }
}