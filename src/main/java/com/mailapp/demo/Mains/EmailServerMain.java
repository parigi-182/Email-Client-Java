package com.mailapp.demo.Mains;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
/*
 * Classe principale del server
 * Crea e lancia la finestra grafica
 */
public class EmailServerMain extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(new File("src/main/resources/com/mailapp/demo/server.fxml").toURI().toURL());
        Scene scene = new Scene(fxmlLoader.load(), 595, 395);
        stage.setTitle("Email server");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(e ->  {
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch();
    }
}