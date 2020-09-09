package main.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.freedesktop.gstreamer.Gst;

public class MainUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("SAMPLE APP");
        Pane mainPane = FXMLLoader.load(MainPageController.class.getResource("MainPage.fxml"));
        Scene mainScene = new Scene(mainPane);
        mainScene.getStylesheets().add("main/ui/application.css");
        primaryStage.setScene(mainScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        // GStreamer Initialization
        String[] gstreamerArgs = new String[1];
        gstreamerArgs[0] = "-v";

        // GStreamer launch
        Gst.init("SAMPLEVideo",gstreamerArgs);

        launch(args);
    }
}
