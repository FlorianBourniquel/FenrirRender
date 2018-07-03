package sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
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
                };
            }
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception{


        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Resource File");
        File selectedDirectory = directoryChooser.showDialog(primaryStage);


        List<CommitVersion> commitVersions = new LinkedList<>();
        convertJsonfileToObject(selectedDirectory,commitVersions);

        Parent root = FXMLLoader.load(getClass().getResource("./view/sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
