package main;

import javafx.application.Application;
import main.ui.MainUI;
import org.freedesktop.gstreamer.Gst;

public class Main {

    public static void main(String[] args)
    {
        // launch JavaFX, blocking call
        Application.launch(MainUI.class, args);
    }
}
