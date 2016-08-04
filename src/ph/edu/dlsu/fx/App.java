package ph.edu.dlsu.fx;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.opencv.core.Core;

public class App extends Application {

    private static final String WINDOW_TITLE = "MY APPLICATION -- Alpha Version";

    private static double displayWidth;
    private static double displayHeight;

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("layout.fxml"));

        initializeScreenSize();

        primaryStage.setTitle(WINDOW_TITLE);
        primaryStage.setScene(new Scene(root, displayWidth, displayHeight));
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    private void initializeScreenSize(){
        ObservableList<Screen> screenList = Screen.getScreens();
        displayWidth = screenList.get(0).getBounds().getWidth();
        displayHeight = screenList.get(0).getBounds().getHeight();
    }


    public static void main(String[] args) {
        // load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}
