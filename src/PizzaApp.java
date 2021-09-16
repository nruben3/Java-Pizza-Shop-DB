// Fig. X: DisplayQueryResults.java
// Main application class that loads and displays the GUI.

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PizzaApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root =
                FXMLLoader.load(getClass().getResource("PizzaGUI.fxml"));

        Scene scene = new Scene(root);
        stage.setTitle("Pizza Orders");
        stage.setScene(scene);
        stage.show();
    }
}