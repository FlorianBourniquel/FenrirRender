package sample.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxDefaultView;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.fx_viewer.util.FxMouseManager;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.graphstream.ui.layout.springbox.implementations.LinLog;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.graphstream.ui.view.util.InteractiveElement;
import sample.model.AntiPatternInstance;
import sample.model.CommitVersion;
import sample.model.PairAPName;
import sample.model.PairAPNameLocation;

import java.net.URL;
import java.util.*;

import static java.lang.Thread.sleep;

public class Controller implements Initializable, ViewerListener {

    private Graph currentGraph = null;

    private boolean loop = true;

    private List<CommitVersion> commitVersions;

    private Map<PairAPName, Map<String, List<PairAPNameLocation>>> classOccurrence;

    private Map<String, Map<String, Integer>> apLocationAndOccurrences = new HashMap<>();

    private Map<String, FxDefaultView> graphsAlreadyCreated = new HashMap<>();

    private CommitVersion currentCommitVersion;

    @FXML
    private Label aPName;

    @FXML
    private ChoiceBox commitVersionChoice;

    @FXML
    private StackPane stackPane;

    @FXML
    private VBox locations;

    @FXML
    private Label occurrences;

    private List<Node> selectedNodes = new LinkedList<>();

    public Controller(List<CommitVersion> commitVersions) {
        this.commitVersions = commitVersions;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setChoiceBoxDataSet();
        createGraphForCommitVersion(commitVersions.get(0).getName());
        commitVersionChoice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                createGraphForCommitVersion(String.valueOf(commitVersionChoice.getItems().get((Integer) number2)));
            }
        });

    }

    private void createGraphForCommitVersion(String name) {
        clearCurrentGraph();
        stackPane.getChildren().clear();
        if (graphsAlreadyCreated.get(name) != null) {
            stackPane.getChildren().add(graphsAlreadyCreated.get(name));
        }
        else {
            for (CommitVersion commitVersionLoop : commitVersions) {
                if (commitVersionLoop.getName().equals(name)) {
                    currentCommitVersion = commitVersionLoop;
                    break;
                }

            }
            currentGraph = new SingleGraph(currentCommitVersion.getName());
            classOccurrence = currentCommitVersion.calculateOccurrenceInSameClass();
            currentGraph.removeAttribute("ui.stylesheet");
            currentGraph.setAttribute("ui.antialias");
            currentGraph.setAttribute("ui.quality");

            FxViewer viewer = new FxViewer(currentGraph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
            viewer.enableAutoLayout(new LinLog());

            for (Map.Entry<String, List<AntiPatternInstance>> entry : currentCommitVersion.getAntiPatterns().entrySet()) {
                if (entry.getValue().size() > 0)
                    currentGraph.addNode(entry.getKey());
            }
            for (Node node : currentGraph) {
                node.setAttribute("ui.label", node.getId());
                node.setAttribute("ui.style", "text-size: 15px; text-alignment: under; text-color: white; text-style: bold; text-background-mode: rounded-box; text-background-color: #222C; text-padding: 5px, 4px; text-offset: 0px, 5px;");

            }

            for (Map.Entry<PairAPName, Map<String, List<PairAPNameLocation>>> entry : classOccurrence.entrySet()) {
                if (entry.getValue().size() > 0) {
                    Edge edge = currentGraph.addEdge(entry.getKey().getName1() + "-" + entry.getKey().getName2(), entry.getKey().getName1(), entry.getKey().getName2(), false);
                    edge.setAttribute("ui.style", "size: " + entry.getValue().size() + "px;");
                }
            }

            //currentGraph.setAttribute("ui.stylesheet", styleSheet);
            FxDefaultView view = (FxDefaultView) viewer.addView("view1", new FxGraphRenderer());
            view.setMouseManager(new FxMouseManager(EnumSet.of(InteractiveElement.EDGE, InteractiveElement.NODE, InteractiveElement.SPRITE)));
            ViewerPipe pipeIn = viewer.newViewerPipe();
            pipeIn.addAttributeSink(currentGraph);
            pipeIn.addViewerListener(this);
            pipeIn.pump();

            graphsAlreadyCreated.put(currentCommitVersion.getName(), view);
            stackPane.getChildren().add(view);

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
    }

    private void clearCurrentGraph() {
        if (currentGraph!=null) {
            for (Node node:selectedNodes) {
                unselectNode(node);
            }
        }
    }

    private void setChoiceBoxDataSet() {
        ObservableList data = FXCollections.observableArrayList();
        for (CommitVersion commitVersion:commitVersions) {
            graphsAlreadyCreated.put(commitVersion.getName(),null);
            data.add(commitVersion.getName());
        }
        commitVersionChoice.setItems(data);
        commitVersionChoice.setValue(data.get(0));
    }

    public void viewClosed(String id) {
        loop = false;
    }

    public void buttonPushed(String id) {
        System.out.println(id);
        Node node = currentGraph.getNode(id);
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
            unselectEdge(selectedNodes.get(0), selectedNodes.get(1));
        selectedNodes.remove(node);
        if (selectedNodes.size() == 1)
            fillInfo(selectedNodes.get(0));
    }

    private void selectNode(Node node) {
        node.setAttribute("ui.style", "fill-color: red;");
        node.setAttribute("selected", "true;");
        selectedNodes.add(node);
        if (selectedNodes.size() == 3) {
            unselectEdge(selectedNodes.get(0), selectedNodes.get(1));
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
                    locations.getChildren().clear();
                    aPName.setText(node.getId());
                    occurrences.setText(String.valueOf(currentCommitVersion.getAntiPatterns().get(node.getId()).size()));
                    ObservableList data = FXCollections.observableArrayList();
                    ListView listView = new ListView();
                    for (AntiPatternInstance ap: currentCommitVersion.getAntiPatterns().get(node.getId())) {
                        data.add(ap.getLocation().toString());
                    }
                    VBox.setVgrow(listView, Priority.ALWAYS);
                    listView.setItems(data);
                    locations.getChildren().add(listView);
                }
        );

    }

    private void fillInfo(Edge edge) {
        String[] names = edge.getId().split("-");
        PairAPName pairAPName = new PairAPName(names[0],names[1]);
        Platform.runLater(
                () -> {
                    locations.getChildren().clear();
                    aPName.setText(edge.getId());
                    occurrences.setText(String.valueOf(classOccurrence.get(pairAPName).size()));
                    for (Map.Entry<String, List<PairAPNameLocation>> entry : classOccurrence.get(pairAPName).entrySet()) {
                        TitledPane titledPane = new TitledPane();
                        titledPane.setText(entry.getKey());
                        VBox vBox = new VBox();
                        for (PairAPNameLocation apNameLocation: entry.getValue()) {
                            Text text = new Text();
                            text.setText(apNameLocation.getName() + " in " + apNameLocation.getLocation().toString());
                            vBox.getChildren().add(text);
                        }
                        titledPane.setContent(vBox);
                        titledPane.setExpanded(true);
                        locations.getChildren().add(titledPane);
                    }
                }
        );

    }

    private void selectEdgeIfExist(Node node, Node node1) {
        Edge edge;
        if (node.getId().compareToIgnoreCase(node1.getId()) < 0)
            edge = currentGraph.getEdge(node.getId() + "-" + node1.getId());
        else
            edge = currentGraph.getEdge(node1.getId() + "-" + node.getId());
        if (edge != null) {
            edge.setAttribute("ui.style", "fill-color: green;");
            fillInfo(edge);
        } else
            fillInfo(node1);
    }

    private void unselectEdge(Node node, Node node1) {
        Edge edge;
        if (node.getId().compareToIgnoreCase(node1.getId()) < 0)
            edge = currentGraph.getEdge(node.getId() + "-" + node1.getId());
        else
            edge = currentGraph.getEdge(node1.getId() + "-" + node.getId());
        if (edge != null) {
            edge.setAttribute("ui.style", "fill-color: black;");
        }
    }
}
