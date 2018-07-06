package sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.gephi.graph.api.*;
import org.gephi.preview.api.*;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxDefaultView;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.fx_viewer.util.FxMouseManager;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.graphstream.ui.view.util.InteractiveElement;
import org.openide.util.Lookup;
import sample.controller.Controller;
import sample.model.AntiPatternInstance;
import sample.model.CommitVersion;
import sample.utils.PreviewSketch;
import sample.utils.TimestampDeserializer;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.List;

import static java.lang.Thread.sleep;

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

        FXMLLoader loader = new FXMLLoader(getClass().getResource("./view/sample.fxml"));

        // Create a controller instance
        Controller controller = new Controller(commitVersions);
        // Set it in the FXMLLoader
        loader.setController(controller);
        Parent root = loader.load();

        primaryStage.setTitle("Graph");
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
