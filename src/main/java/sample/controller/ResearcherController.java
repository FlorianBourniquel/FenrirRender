package sample.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
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
import sample.model.*;
import sample.utils.MyPredicate;
import sample.utils.PairColorRange;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.Thread.sleep;

public class ResearcherController implements Initializable, ViewerListener {

    private Graph currentGraph = null;

    private String allCommitVersionName;

    private boolean loop = true;

    private List<CommitVersion> commitVersions;

    private Map<PairAPName, Map<String, List<PairAPDataLocation>>> apOccurrence;

    private Map<String, Map<String, Integer>> apLocationAndOccurrences = new HashMap<>();

    private Map<String, Map<String, List<Boolean>>> statusAPSelected = new HashMap<>();

    private Map<String, Map<String, Map<PairAPName, Map<String, List<PairAPDataLocation>>>>> occurrenceAlreadyCalculated = new HashMap<>();

    private Map<String, MyPredicate> predicateMap = new HashMap<>();

    private List<PairColorRange> colorRanges = new LinkedList<>();

    private List<CommitVersion> currentCommitVersionList = new LinkedList<>();

    private ToggleGroup scopeGroup = new ToggleGroup();

    private ToggleGroup styleGroup = new ToggleGroup();

    private String previousScope = "Class";

    @FXML
    private Label aPName;

    @FXML
    private RadioButton classScopeButton;

    @FXML
    private GridPane gridPaneLeft;

    @FXML
    private RadioButton colorStyleButton;

    @FXML
    private RadioButton thicknessStyleButton;

    @FXML
    private Button clearNotLinked;

    @FXML
    private Button exportAllButton;

    @FXML
    private Button exportLocationButton;

    @FXML
    private RadioButton functionScopeButton;

    @FXML
    private RadioButton lineScopeButton;

    @FXML
    private TitledPane commitVersionChoice;

    @FXML
    private StackPane stackPane;

    @FXML
    private FlowPane legend;

    @FXML
    private VBox locations;

    @FXML
    private FlowPane flowPaneCommitVersions;

    @FXML
    private HBox apActivatedHbox;

    @FXML
    private Label occurrences;

    private List<Node> selectedNodes = new LinkedList<>();

    private Boolean block = false;

    public ResearcherController(List<CommitVersion> commitVersions) {
        this.commitVersions = commitVersions;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        legend.managedProperty().bind(legend.visibleProperty());
        classScopeButton.setToggleGroup(scopeGroup);
        functionScopeButton.setToggleGroup(scopeGroup);
        lineScopeButton.setToggleGroup(scopeGroup);
        classScopeButton.setSelected(true);
        thicknessStyleButton.setToggleGroup(styleGroup);
        colorStyleButton.setToggleGroup(styleGroup);
        colorStyleButton.setSelected(true);
        setLegend();
        setCommitChoiceDataSet();
        predicateMap.put("Class", Location::isSameClass);
        predicateMap.put("Function", Location::isSameClassAndFunction);
        predicateMap.put("Line", Location::isSame);
        createGraphForCommitVersion();
        commitVersionChoice.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
                gridPaneLeft.getRowConstraints().get(0).setPercentHeight(110);
            else
                gridPaneLeft.getRowConstraints().get(0).setPercentHeight(3);
        });
        scopeGroup.selectedToggleProperty().addListener((ov, old_toggle, new_toggle) -> {
            if (scopeGroup.getSelectedToggle() != null) {
                previousScope = String.valueOf(((RadioButton) old_toggle).getText());
                createGraphForCommitVersion();
            }
        });
        styleGroup.selectedToggleProperty().addListener((ov, old_toggle, new_toggle) -> {
            if (styleGroup.getSelectedToggle() != null) {
                String s = String.valueOf(((RadioButton) new_toggle).getText());
                if (s.equals("Color"))
                    switchToColorStyle();
                else
                    switchToThicknessStyle();
            }
        });
        clearNotLinked.setOnAction((event) -> {
            for (javafx.scene.Node node: apActivatedHbox.getChildren()) {
               RadioButton radioButton = ((RadioButton) node);
               Optional<Edge> optionalEdge = currentGraph.edges().filter(n -> n.getNode1().getId().equals(((Text) radioButton.getGraphic()).getText()) || n.getNode0().getId().equals(((Text) radioButton.getGraphic()).getText())).findFirst();
               if (!optionalEdge.isPresent())
                   radioButton.setSelected(false);
            }
        });
        exportAllButton.setOnAction((event) -> {
            try {
                FileChooser fileChooser = new FileChooser();

                //Set extension filter
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.txt)", "*.csv");
                fileChooser.getExtensionFilters().add(extFilter);

                //Show save file dialog
                File file = fileChooser.showSaveDialog(new Stage());

                if (file != null) {

                    BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getAbsolutePath()));
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("AP Name").withAllowMissingColumnNames());

                    for (Map.Entry<PairAPName, Map<String, List<PairAPDataLocation>>> entry : apOccurrence.entrySet()) {
                        List<String> list = new LinkedList<>();
                        list.add(entry.getKey().toString());
                        for (Map.Entry<String, List<PairAPDataLocation>> subEntry : entry.getValue().entrySet()) {
                            for (PairAPDataLocation apNameLocation : subEntry.getValue()) {
                                list.add("\"" + apNameLocation.getName() + " in " + apNameLocation.getLocation().toString() + "\"");
                            }
                        }
                        csvPrinter.printRecord(list);
                    }
                    csvPrinter.flush();
                    csvPrinter.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        exportLocationButton.setOnAction((event) -> {
            try {
                FileChooser fileChooser = new FileChooser();

                //Set extension filter
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.txt)", "*.csv");
                fileChooser.getExtensionFilters().add(extFilter);

                //Show save file dialog
                File file = fileChooser.showSaveDialog(new Stage());

                if (file != null) {

                    BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getAbsolutePath()));

                    if (selectedNodes.size() >= 2) {
                        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withAllowMissingColumnNames());
                        for (javafx.scene.Node titledPane: locations.getChildren()) {
                            List<String> list = new LinkedList<>();
                            list.add(((TitledPane) titledPane).getText());
                            VBox vBox = (VBox) ((TitledPane) titledPane).getContent();
                            for (javafx.scene.Node text : vBox.getChildren()) {
                                list.add(((Text) text).getText());
                            }
                            csvPrinter.printRecord(list);
                        }
                        csvPrinter.flush();
                        csvPrinter.close();
                    }
                    else if (selectedNodes.size() >= 1) {
                        ListView listView = (ListView) locations.getChildren().get(0);
                        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader( aPName.getText() + " locations").withAllowMissingColumnNames());
                        List<String> list = listView.getItems();
                        for (String s : list) {
                            csvPrinter.printRecord(s);
                        }
                        csvPrinter.flush();
                        csvPrinter.close();
                    }



                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void switchToColorStyle() {
        for (int i = 0; i < currentGraph.getEdgeCount(); i++) {
            currentGraph.getEdge(i).setAttribute("ui.style", "size: " + 4 + "px;");
            currentGraph.getEdge(i).setAttribute("ui.style", currentGraph.getEdge(i).getAttribute("color"));
        }
        legend.setVisible(true);
    }

    private void switchToThicknessStyle() {
        for (int i = 0; i < currentGraph.getEdgeCount(); i++) {
            currentGraph.getEdge(i).setAttribute("ui.style", "fill-color: black;");
            currentGraph.getEdge(i).setAttribute("ui.style", currentGraph.getEdge(i).getAttribute("size"));
        }
        legend.setVisible(false);
    }

    private void setLegend() {
        colorRanges.add(new PairColorRange(0, 4, "#729ea1"));
        colorRanges.add(new PairColorRange(5, 9, "#b5bd89"));
        colorRanges.add(new PairColorRange(10, 14, "#dfbe99"));
        colorRanges.add(new PairColorRange(15, 19, "#ec9192"));
        colorRanges.add(new PairColorRange(20, Integer.MAX_VALUE, "#db5375"));
        for (PairColorRange colorRange : colorRanges) {
            Rectangle rectangle = new Rectangle();
            rectangle.setHeight(20.0);
            rectangle.setWidth(20.0);
            rectangle.setFill(Color.web(colorRange.getColor()));
            Label label = new Label();
            if (colorRange.getMin() <= 0)
                label.setText("<" + colorRange.getMax());
            else if (colorRange.getMax() == Integer.MAX_VALUE)
                label.setText(">" + colorRange.getMin());
            else
                label.setText("[" + colorRange.getMin() + " - " + colorRange.getMax() + "]");
            legend.getChildren().add(rectangle);
            legend.getChildren().add(label);
        }

    }

    private void setAPActivatedHboxDataSet() {
        apActivatedHbox.getChildren().clear();
        List<RadioButton> radioButtonList = new LinkedList<>();
        for (CommitVersion commitVersion : currentCommitVersionList) {
            for (Map.Entry<String, List<AntiPatternInstance>> entry : commitVersion.getAntiPatterns().entrySet()) {
                Optional<RadioButton> matchingObject = radioButtonList.stream().filter(p -> p.getText().equals(entry.getKey())).findFirst();
                if (matchingObject.isPresent())
                    continue;
                RadioButton radioButton = new RadioButton();
                radioButton.setGraphic(new Text(entry.getKey()));
                HBox.setHgrow(radioButton, Priority.ALWAYS);
                radioButton.setAlignment(Pos.CENTER);
                radioButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                radioButton.setSelected(true);
                radioButton.selectedProperty().addListener((obs, wasPreviouslySelected, isNowSelected) -> {
                    if (!block) {
                        if (isNowSelected) {
                            addAPToGraph(((Text)radioButton.getGraphic()).getText());
                        } else {
                            removeAPToGraph(((Text)radioButton.getGraphic()).getText());
                        }
                    }
                });
                radioButtonList.add(radioButton);
                apActivatedHbox.getChildren().add(radioButton);
            }


        }
    }

    private void removeAPToGraph(String text) {
        for (int i = 0; i < currentGraph.getEdgeCount(); i++) {
            if (currentGraph.getEdge(i).getNode0().getId().equals(text) || currentGraph.getEdge(i).getNode1().getId().equals(text)) {
                unselectEdge(currentGraph.getEdge(i).getNode0(), currentGraph.getEdge(i).getNode1());
                currentGraph.removeEdge(i);
            }
        }
        unselectNode(currentGraph.getNode(text));
        currentGraph.removeNode(text);
    }

    private void addAPToGraph(String text) {
        currentGraph.addNode(text);
        setCssToNode();
        createEdge(occurrenceAlreadyCalculated.get(allCommitVersionName).get(String.valueOf(((RadioButton) scopeGroup.getSelectedToggle()).getText())), text);
    }

    private void createGraphForCommitVersion(String... args) {
        clearCurrentGraphAndSaveStatus();
        currentCommitVersionList.clear();
        for (javafx.scene.Node commitVersionLoop : flowPaneCommitVersions.getChildren()) {
            RadioButton radioButton = (RadioButton) commitVersionLoop;
            if (radioButton.isSelected()) {
                Optional<CommitVersion> matchingObject = commitVersions.stream().filter(p -> p.getName().equals(radioButton.getText())).findFirst();
                matchingObject.ifPresent(commitVersion -> currentCommitVersionList.add(commitVersion));
            }
        }

        setAPActivatedHboxDataSet();
        String scope = String.valueOf(((RadioButton) scopeGroup.getSelectedToggle()).getText());

        StringBuilder nameBuilder = new StringBuilder();
        for (CommitVersion commitVersion : currentCommitVersionList) {
            nameBuilder.append(commitVersion.getName()).append("-");
        }
        allCommitVersionName = nameBuilder.toString();
        currentGraph = new SingleGraph(allCommitVersionName + "-" + scope);
        if (occurrenceAlreadyCalculated.containsKey(allCommitVersionName) && occurrenceAlreadyCalculated.get(allCommitVersionName).containsKey(scope))
            apOccurrence = occurrenceAlreadyCalculated.get(allCommitVersionName).get(scope);
        else {
            apOccurrence = CommitVersion.calculateOccurrence(predicateMap.get(scope), currentCommitVersionList);

            if (occurrenceAlreadyCalculated.containsKey(allCommitVersionName)) {
                occurrenceAlreadyCalculated.get(allCommitVersionName).put(scope, apOccurrence);
            } else {
                HashMap<String, Map<PairAPName, Map<String, List<PairAPDataLocation>>>> hashMap = new HashMap<>();
                hashMap.put(scope, apOccurrence);
                occurrenceAlreadyCalculated.put(allCommitVersionName, hashMap);
            }
        }

        currentGraph.removeAttribute("ui.stylesheet");
        currentGraph.setAttribute("ui.antialias");
        currentGraph.setAttribute("ui.quality");
        FxViewer viewer = new FxViewer(currentGraph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout(new LinLog());

        List<String> nodeAlreadyCreated = new LinkedList<>();
        for (CommitVersion commitVersion : currentCommitVersionList) {
            for (Map.Entry<String, List<AntiPatternInstance>> entry : commitVersion.getAntiPatterns().entrySet()) {
                if (entry.getValue().size() > 0 && !nodeAlreadyCreated.contains(entry.getKey())) {
                    currentGraph.addNode(entry.getKey());
                    nodeAlreadyCreated.add(entry.getKey());
                }
            }
        }

        setCssToNode();

        createEdge(apOccurrence);
        if (statusAPSelected.containsKey(allCommitVersionName) && statusAPSelected.get(allCommitVersionName).containsKey(scope)) {
            List<Boolean> booleans = statusAPSelected.get(allCommitVersionName).get(scope);
            for (int i = 0; i < booleans.size(); i++) {
                ((RadioButton) apActivatedHbox.getChildren().get(i)).setSelected(booleans.get(i));
            }
        }


        //currentGraph.setAttribute("ui.stylesheet", styleSheet);
        FxDefaultView view = (FxDefaultView) viewer.addView("view1", new FxGraphRenderer());
        view.setMouseManager(new FxMouseManager(EnumSet.of(InteractiveElement.EDGE, InteractiveElement.NODE, InteractiveElement.SPRITE)));
        ViewerPipe pipeIn = viewer.newViewerPipe();
        pipeIn.addAttributeSink(currentGraph);
        pipeIn.addViewerListener(this);
        pipeIn.pump();

        if (stackPane.getChildren().size() >= 2)
            stackPane.getChildren().remove(0);
        stackPane.getChildren().add(0, view);


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

    private void setCssToNode() {
        for (Node node : currentGraph) {
            node.setAttribute("ui.label", node.getId());
            node.setAttribute("ui.style", "text-size: 15px; text-alignment: under; text-color: white; text-style: bold; text-background-mode: rounded-box; text-background-color: #222C; text-padding: 5px, 4px; text-offset: 0px, 5px;");

        }
    }

    private void createEdge(Map<PairAPName, Map<String, List<PairAPDataLocation>>> apOccurrence, String... computeOnly) {
        for (Map.Entry<PairAPName, Map<String, List<PairAPDataLocation>>> entry : apOccurrence.entrySet()) {
            if (computeOnly.length > 0 && !entry.getKey().getName1().equals(computeOnly[0]) && !entry.getKey().getName2().equals(computeOnly[0]))
                continue;

            if (entry.getValue().size() > 0 && currentGraph.getNode(entry.getKey().getName2()) != null && currentGraph.getNode(entry.getKey().getName1()) != null) {
                Edge edge = currentGraph.addEdge(entry.getKey().getName1() + "-" + entry.getKey().getName2(), entry.getKey().getName1(), entry.getKey().getName2(), false);
                for (PairColorRange colorRange : colorRanges) {
                    if (entry.getValue().size() >= colorRange.getMin() && entry.getValue().size() <= colorRange.getMax()) {
                        edge.setAttribute("color", "fill-color: " + colorRange.getColor() + ";");
                        edge.setAttribute("size", "size: " + entry.getValue().size() + "px;");
                        break;
                    }
                }
            }
        }
        String s = String.valueOf(((RadioButton) styleGroup.getSelectedToggle()).getText());
        if (s.equals("Color"))
            switchToColorStyle();
        else
            switchToThicknessStyle();
    }

    private void clearCurrentGraphAndSaveStatus() {
        if (currentGraph != null) {
            if (!statusAPSelected.containsKey(allCommitVersionName))
                statusAPSelected.put(allCommitVersionName, new HashMap<>());
            if (statusAPSelected.get(allCommitVersionName).containsKey(previousScope))
                statusAPSelected.get(allCommitVersionName).get(previousScope).clear();
            List<Boolean> booleans = new LinkedList<>();
            for (javafx.scene.Node radioButton : apActivatedHbox.getChildren()) {
                booleans.add(((RadioButton) radioButton).isSelected());
            }
            statusAPSelected.get(allCommitVersionName).put(previousScope, booleans);
            for (Node node : selectedNodes) {
                unselectNode(node);
            }
        }
    }

    private void setCommitChoiceDataSet() {
        for (int i = 0; i < commitVersions.size(); i++) {
            RadioButton radioButton = new RadioButton();
            radioButton.setText(commitVersions.get(i).getName());
            if (i == 0)
                radioButton.setSelected(true);
            radioButton.selectedProperty().addListener((obs, wasPreviouslySelected, isNowSelected) -> {
                while (selectedNodes.size() > 0) {
                    unselectNode(selectedNodes.get(0));
                }
                createGraphForCommitVersion();
            });
            flowPaneCommitVersions.getChildren().add(radioButton);
        }
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
        node.setAttribute("ui.style", "fill-color: #9d54ea;");
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
                    exportLocationButton.setDisable(false);
                    exportLocationButton.setVisible(true);
                    ObservableList data = FXCollections.observableArrayList();
                    locations.getChildren().clear();
                    aPName.setText(node.getId());
                    for (CommitVersion commitVersion : currentCommitVersionList) {
                        for (AntiPatternInstance ap : commitVersion.getAntiPatterns().get(node.getId())) {
                            data.add(ap.getLocation().toString());
                        }
                    }
                    ListView listView = new ListView();
                    VBox.setVgrow(listView, Priority.ALWAYS);
                    listView.setItems(data);
                    locations.getChildren().add(listView);
                    occurrences.setText(String.valueOf(data.size()));
                }
        );

    }

    private void fillInfo(Edge edge) {
        String[] names = edge.getId().split("-");
        PairAPName pairAPName = new PairAPName(names[0], names[1]);
        Platform.runLater(
                () -> {
                    locations.getChildren().clear();
                    aPName.setText(edge.getId());
                    occurrences.setText(String.valueOf(apOccurrence.get(pairAPName).size()));

                    for (Map.Entry<String, List<PairAPDataLocation>> entry : apOccurrence.get(pairAPName).entrySet()) {
                        TitledPane titledPane = new TitledPane();
                        titledPane.setText(entry.getKey());
                        VBox vBox = new VBox();
                        HashMap<String, TitledPane> titledPaneHashMap = new HashMap<>();
                        for (PairAPDataLocation apNameLocation : entry.getValue()) {
                            Text text = new Text();
                            text.setText(apNameLocation.getName() + " in " + apNameLocation.getLocation().toString());
                            if (titledPaneHashMap.containsKey(apNameLocation.getLocation().getFunctionLocation())) {
                                ((VBox) titledPaneHashMap.get(apNameLocation.getLocation().getFunctionLocation()).getContent()).getChildren().add(text);
                            }
                            else {
                                VBox vBox1 = new VBox();
                                TitledPane titledPane1 = new TitledPane();
                                titledPane1.setExpanded(false);
                                vBox1.getChildren().add(text);
                                titledPane1.setContent(vBox1);
                                if (apNameLocation.getLocation().getFunctionLocation().equals(""))
                                    titledPane1.setText("Unknown");
                                else
                                    titledPane1.setText(apNameLocation.getLocation().getFunctionLocation());
                                titledPaneHashMap.put(apNameLocation.getLocation().getFunctionLocation(),titledPane1);
                            }
                        }
                        for (Map.Entry<String, TitledPane> entry1 : titledPaneHashMap.entrySet()) {
                            vBox.getChildren().add(entry1.getValue());
                        }
                        titledPane.setContent(vBox);
                        titledPane.setExpanded(false);
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
            edge.setAttribute("ui.style", "fill-color: #372248;");
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
            edge.setAttribute("ui.style", edge.getAttribute("color"));
        }
    }
}
