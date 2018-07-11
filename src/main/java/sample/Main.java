package sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import sample.controller.MainController;
import sample.controller.ResearcherController;
import sample.model.CommitVersion;
import sample.utils.TimestampDeserializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Main extends Application {



    protected static String styleSheet = "graph         { padding: 20px; stroke-width: 0px; }"
            + "node:selected { fill-color: red;  fill-mode: plain; }"
            + "node:clicked  { fill-color: blue; fill-mode: plain; }"
            + "edge:selected { fill-color: purple; fill-mode: plain; }"
            + "edge:clicked  { fill-color: orange; fill-mode: plain; }";

    private void convertJsonfileToObject(final File folder, List<CommitVersion> commitVersions) {
        BufferedReader br = null;
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Calendar.class, new TimestampDeserializer());
        Gson gsonObj = gsonBuilder.create();
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (!fileEntry.isDirectory()) {
                System.out.println(fileEntry.getName());
                try {
                    br = new BufferedReader(new FileReader(fileEntry.getAbsolutePath()));
                    CommitVersion commitVersion = gsonObj.fromJson(br, CommitVersion.class);
                    commitVersions.add(commitVersion);
                } catch (FileNotFoundException e) {

                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.setProperty("org.graphstream.ui", "org.graphstream.ui.javafx.util.Display");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Resource File");
        File selectedDirectory = directoryChooser.showDialog(primaryStage);

        List<CommitVersion> commitVersions = new LinkedList<>();
        convertJsonfileToObject(selectedDirectory, commitVersions);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/main.fxml"));

        // Create a researcherController instance
        MainController mainController = new MainController(commitVersions);
        // Set it in the FXMLLoader
        loader.setController(mainController);
        Parent root = loader.load();

        primaryStage.setTitle("Mode Choice");
        Scene scene = new Scene(root, 1920, 1080);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

}
