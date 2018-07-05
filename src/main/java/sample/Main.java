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

public class Main extends Application implements ViewerListener {


    private Graph graph;
    private List<CommitVersion> commitVersions = new LinkedList<>();
    private Map<String, Map<String, Integer>> classOccurrence;
    private CommitVersion commitVersion;
    private Label aPName;
    private Label occurrences;
    private List<Node> selectedNodes = new LinkedList<>();

    protected static String styleSheet = "graph         { padding: 20px; stroke-width: 0px; }"
            + "node:selected { fill-color: red;  fill-mode: plain; }"
            + "node:clicked  { fill-color: blue; fill-mode: plain; }"
            + "edge:selected { fill-color: purple; fill-mode: plain; }"
            + "edge:clicked  { fill-color: orange; fill-mode: plain; }";
    private boolean loop = true;

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


        convertJsonfileToObject(selectedDirectory, commitVersions);
        commitVersion = commitVersions.get(0);
        classOccurrence = commitVersion.calculateOccurenceInSameClass();


        Parent root = FXMLLoader.load(getClass().getResource("./view/sample.fxml"));
        primaryStage.setTitle("Graph");
        Scene scene = new Scene(root, 1920, 1080);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });


        graph = new SingleGraph(commitVersion.getName());
        graph.removeAttribute("ui.stylesheet");
        graph.setAttribute("ui.antialias");
        graph.setAttribute("ui.quality");

        FxViewer viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout();

        for (Map.Entry<String, List<AntiPatternInstance>> entry : commitVersion.getAntiPatterns().entrySet()) {
            if (entry.getValue().size() > 0)
                graph.addNode(entry.getKey());
        }
        for (Node node : graph) {
            node.setAttribute("ui.label", node.getId());
            node.setAttribute("ui.style", "text-size: 15px; text-alignment: under; text-color: white; text-style: bold; text-background-mode: rounded-box; text-background-color: #222C; text-padding: 5px, 4px; text-offset: 0px, 5px;");

        }
        List<String> alreadyProcessed = new LinkedList<>();
        for (Map.Entry<String, Map<String, Integer>> entry : classOccurrence.entrySet()) {

            for (Map.Entry<String, Integer> entryTmp : entry.getValue().entrySet()) {
                if (!alreadyProcessed.contains(entryTmp.getKey()) && entryTmp.getValue() > 0) {
                    Edge edge;
                    if (entry.getKey().compareToIgnoreCase(entryTmp.getKey()) < 0)
                        edge = graph.addEdge(entry.getKey() + "-" + entryTmp.getKey(), entry.getKey(), entryTmp.getKey(), false);
                    else
                        edge = graph.addEdge(entryTmp.getKey() + "-" + entry.getKey(), entry.getKey(), entryTmp.getKey(), false);
                    edge.setAttribute("ui.style", "size: " + entryTmp.getValue() + "px;");
                }
            }
            alreadyProcessed.add(entry.getKey());
        }

        //graph.setAttribute("ui.stylesheet", styleSheet);
        FxDefaultView view = (FxDefaultView) viewer.addView("view1", new FxGraphRenderer());
        view.setMouseManager(new FxMouseManager(EnumSet.of(InteractiveElement.EDGE, InteractiveElement.NODE, InteractiveElement.SPRITE)));
        ViewerPipe pipeIn = viewer.newViewerPipe();
        pipeIn.addAttributeSink(graph);
        pipeIn.addViewerListener(this);
        pipeIn.pump();


        StackPane gridPane = (StackPane) scene.lookup("#pane");
        ListView listView = (ListView) scene.lookup("#avb");
        aPName = (Label) scene.lookup("#APName");
        occurrences = (Label) scene.lookup("#Occurrences");
        ObservableList data =
                FXCollections.observableArrayList();
        data.addAll(
                "Adam", "Alex", "Alfred", "Albert",
                "Brenda", "Connie", "Derek", "Donny",
                "Lynne", "Myrtle", "Rose", "Rudolph",
                "Tony", "Trudy", "Williams", "Zach"
        );
        listView.setItems(data);
        gridPane.getChildren().add(view);
        primaryStage.show();

        new Thread(() -> {
            while (loop) {
                pipeIn.pump();
                try {
                    sleep(40);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }).start();

    }


    public static void main(String[] args) {
        launch(args);
    }

    // Viewer Listener Interface

    public void viewClosed(String id) {
        loop = false;
    }

    public void buttonPushed(String id) {
        System.out.println(id);
        Node node = graph.getNode(id);
        if (node.hasAttribute("selected")) {
            unselectNode(node);
        } else {
            selectNode(node);
        }
    }

    public void buttonReleased(String id) {
    }

    public void mouseOver(String id) {
    }

    public void mouseLeft(String id) {
    }

    private void unselectNode(Node node) {
        node.setAttribute("ui.style", "fill-color: black;");
        node.removeAttribute("selected");
        if (selectedNodes.size() == 2)
            unselectEdgeIfExist(selectedNodes.get(0), selectedNodes.get(1));
        selectedNodes.remove(node);
        if (selectedNodes.size() == 1)
            fillInfo(selectedNodes.get(0));
    }

    private void selectNode(Node node) {
        node.setAttribute("ui.style", "fill-color: red;");
        node.setAttribute("selected", "true;");
        selectedNodes.add(node);
        if (selectedNodes.size() == 3) {
            unselectEdgeIfExist(selectedNodes.get(0), selectedNodes.get(1));
            unselectNode(selectedNodes.get(0));
        }
        if (selectedNodes.size() == 2)
            selectEdgeIfExist(selectedNodes.get(0), selectedNodes.get(1));
        else
            fillInfo(node);
    }

    private void fillInfo(Node node) {
        Platform.runLater(
            () -> {
                aPName.setText(node.getId());
                occurrences.setText(String.valueOf(commitVersion.getAntiPatterns().get(node.getId()).size()));
            }
        );

    }

    private void fillInfo(Edge edge) {
        String[] names = edge.getId().split("-");
        Platform.runLater(
                () -> {
                    aPName.setText(edge.getId());
                    occurrences.setText(String.valueOf(classOccurrence.get(names[0]).get(names[1])));
                }
        );

    }

    private void selectEdgeIfExist(Node node, Node node1) {
        Edge edge;
        if (node.getId().compareToIgnoreCase(node1.getId()) < 0)
            edge = graph.getEdge(node.getId() + "-" + node1.getId());
        else
            edge = graph.getEdge(node1.getId() + "-" + node.getId());
        if (edge != null) {
            edge.setAttribute("ui.style", "fill-color: green;");
            fillInfo(edge);
        } else
            fillInfo(node1);
    }

    private void unselectEdgeIfExist(Node node, Node node1) {
        Edge edge;
        if (node.getId().compareToIgnoreCase(node1.getId()) < 0)
            edge = graph.getEdge(node.getId() + "-" + node1.getId());
        else
            edge = graph.getEdge(node1.getId() + "-" + node.getId());
        if (edge != null) {
            edge.setAttribute("ui.style", "fill-color: black;");
        }
    }


}
