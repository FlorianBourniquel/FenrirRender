package sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
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
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.openide.util.Lookup;
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

public class Main extends Application {

    private String styleSheet = ""
            + "graph {"
            + "	canvas-color: white; "
            + "	fill-mode: gradient-radial; "
            + "	fill-color: white, #EEEEEE; "
            + "	padding: 60px; "
            + "}"
            + ""
            + "node {"
            + "	shape: freeplane;"
            + "	size: 10px;"
            + "	size-mode: fit;"
            + "	fill-mode: none;"
            + "	stroke-mode: plain;"
            + "	stroke-color: grey;"
            + "	stroke-width: 3px;"
            + "	padding: 5px, 1px;"
            + "	shadow-mode: none;"
            + "	icon-mode: at-left;"
            + "	text-style: normal;"
            + "	text-font: 'Droid Sans';"
            + ""
            + "node:clicked {"
            + "	stroke-mode: plain;"
            + "	stroke-color: red;"
            + "}"
            + ""
            + "node:selected {"
            + "	stroke-mode: plain;"
            + "	stroke-color: blue;"
            + "}"
            + ""
            + "edge {"
            + "	shape: freeplane;"
            + "	size: 3px;"
            + "	fill-color: grey;"
            + "	fill-mode: plain;"
            + "	shadow-mode: none;"
            + "	shadow-color: rgba(0,0,0,100);"
            + "	shadow-offset: 3px, -3px;"
            + "	shadow-width: 0px;"
            + "	arrow-shape: arrow;"
            + "	arrow-size: 20px, 6px;"
            + "}";

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


        Parent root = FXMLLoader.load(getClass().getResource("./view/sample.fxml"));
        /*primaryStage.setTitle("Hello World");
        Scene scene = new Scene( root,300, 275);
        primaryStage.setScene(scene);
        primaryStage.show();*/

        CommitVersion commitVersion = commitVersions.get(0);
        Map<String, Map<String, Integer>> classOccurence = commitVersion.calculateOccurenceInSameClass();
        Graph graph = new SingleGraph(commitVersion.getName());
        graph.removeAttribute("ui.stylesheet");





        graph.setAttribute("ui.antialias");
        graph.setAttribute("ui.quality");




        // graphLink.showGraphs();

        FxViewer viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout();
        for (Map.Entry<String, List<AntiPatternInstance>> entry : commitVersion.getAntiPatterns().entrySet()) {
            graph.addNode(entry.getKey());
        }
        for (Node node : graph) {
            node.setAttribute("ui.label", node.getId());
            node.setAttribute("ui.style", "text-size: 15px; text-alignment: under; text-color: white; text-style: bold; text-background-mode: rounded-box; text-background-color: #222C; text-padding: 5px, 4px; text-offset: 0px, 5px;");

        }

        for (Map.Entry<String, Map<String, Integer>> entry : classOccurence.entrySet()) {
            for (Map.Entry<String, Integer> entryTmp : entry.getValue().entrySet()) {
                Edge edge = graph.addEdge(entry.getKey() + "-" + entryTmp.getKey(), entry.getKey(), entryTmp.getKey(), false);
                edge.setAttribute("ui.style", "size: " + entryTmp.getValue() + "px;");
            }
        }
        //graph.setAttribute("ui.stylesheet", styleSheet);
        FxViewPanel panel = (FxViewPanel) viewer.addDefaultView(false, new FxGraphRenderer());

        Scene scene = new Scene(panel, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }



}
