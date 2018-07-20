package sample.controller;

import afester.javafx.svg.SvgLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import sample.model.AntiPatternInstance;
import sample.model.CommitVersion;
import sample.utils.SVGRange;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class ArchitectController implements Initializable {

    private List<CommitVersion> commitVersions;
    private List<String> classNames = new LinkedList<>();
    private List<CommitVersion> currentCommitVersions = new LinkedList<>();
    private List<SVGRange> svgRanges = new LinkedList<>();
    private int currentIndexPackage = 0;

    @FXML
    private ChoiceBox projectChoice;

    @FXML
    private Slider commitSlider;

    @FXML
    private FlowPane classFlowPane;

    @FXML
    private StackPane stackPane;

    public ArchitectController(List<CommitVersion> commitVersions) {
        this.commitVersions = commitVersions;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setSVGRanges();
        commitVersions.sort(Comparator.comparing(CommitVersion::getDate));
        setProjectChoiceDataSet();
        setClassFlowPane();
        commitSlider.valueProperty().addListener((ov, oldVal, newVal) -> {
            commitSlider.setValue(newVal.intValue());
            if (oldVal.intValue() != newVal.intValue())
                setClassFlowPane();
        });

    }

    private void setSVGRanges() {
        svgRanges.add(new SVGRange(0, 0, 0, "../../building-0-0.svg"));
        svgRanges.add(new SVGRange(1, 5, 0, "../../building-1-5.svg"));
        svgRanges.add(new SVGRange(6, 10, 0, "../../building-6-10.svg"));
        svgRanges.add(new SVGRange(11, 15, 0, "../../building-11-15.svg"));
        svgRanges.add(new SVGRange(16, 20, 0, "../../building-16-20.svg"));
        svgRanges.add(new SVGRange(21, 25, 0, "../../building-21-25.svg"));
        svgRanges.add(new SVGRange(26, 30, 0, "../../building-26-30.svg"));
        svgRanges.add(new SVGRange(31, 35, 0, "../../building-31-35.svg"));
        svgRanges.add(new SVGRange(36, Integer.MAX_VALUE, 0, "../../building-36-40.svg"));
        svgRanges.add(new SVGRange(1, 5, 1, "../../building-1-5-fire.svg"));
        svgRanges.add(new SVGRange(6, 10, 1, "../../building-6-10-fire.svg"));
        svgRanges.add(new SVGRange(11, 15, 1, "../../building-11-15-fire.svg"));
        svgRanges.add(new SVGRange(16, 20, 1, "../../building-16-20-fire.svg"));
        svgRanges.add(new SVGRange(21, 26, 1, "../../building-21-25-fire.svg"));
        svgRanges.add(new SVGRange(26, 30, 1, "../../building-26-30-fire.svg"));
        svgRanges.add(new SVGRange(31, 35, 1, "../../building-31-35-fire.svg"));
        svgRanges.add(new SVGRange(36, Integer.MAX_VALUE, 1, "../../building-36-40-fire.svg"));
        svgRanges.add(new SVGRange(1, 5, -1, "../../building-1-5-heal.svg"));
        svgRanges.add(new SVGRange(6, 10, -1, "../../building-6-10-heal.svg"));
        svgRanges.add(new SVGRange(11, 15, -1, "../../building-11-15-heal.svg"));
        svgRanges.add(new SVGRange(16, 20, -1, "../../building-16-20-heal.svg"));
        svgRanges.add(new SVGRange(21, 26, -1, "../../building-21-25-heal.svg"));
        svgRanges.add(new SVGRange(26, 30, -1, "../../building-26-30-heal.svg"));
        svgRanges.add(new SVGRange(31, 35, -1, "../../building-31-35-heal.svg"));
        svgRanges.add(new SVGRange(36, Integer.MAX_VALUE, -1, "../../building-36-40-heal.svg"));
    }

    private void setClassFlowPane() {
        int index = (int) commitSlider.getValue();
        Map<String, List<AntiPatternInstance>> apByClasses = currentCommitVersions.get(index).getApByClasses();
        if (currentIndexPackage == 0) {
            Object[] strings = apByClasses.keySet().toArray();
            if (strings.length > 2)
                currentIndexPackage = lastIndexOfSameSubSeq(strings[0].toString(), strings[1].toString());
        }
        for (Map.Entry<String, List<AntiPatternInstance>> entry : apByClasses.entrySet()) {
            if (!classNames.contains(entry.getKey()))
                classNames.add(entry.getKey());

        }
        classFlowPane.getChildren().clear();
        for (String className : classNames) {
            int isFireHealOrNeutral = 0;
            if (index > 0) {
                if (currentCommitVersions.get(index - 1).getApByClasses().containsKey(className) && currentCommitVersions.get(index).getApByClasses().containsKey(className)) {
                    if (currentCommitVersions.get(index - 1).getApByClasses().get(className).size() < currentCommitVersions.get(index).getApByClasses().get(className).size())
                        isFireHealOrNeutral = 1;
                    else if (currentCommitVersions.get(index - 1).getApByClasses().get(className).size() > currentCommitVersions.get(index).getApByClasses().get(className).size())
                        isFireHealOrNeutral = -1;
                }
                else if (!currentCommitVersions.get(index - 1).getApByClasses().containsKey(className))
                    isFireHealOrNeutral = 1;
            }
            final int finalIsFireHealOrNeutral = isFireHealOrNeutral;

            InputStream svgFile = getClass()
                    .getResourceAsStream(svgRanges.stream()
                            .filter(svgRange -> apByClasses.containsKey(className)
                                    && svgRange.isFireHealOrNeutral() == finalIsFireHealOrNeutral
                                    && svgRange.getMax() >= apByClasses.get(className).size()
                                    && svgRange.getMin() <= apByClasses.get(className).size())
                            .findFirst()
                            .orElse(svgRanges.get(0)).getPath());
            SvgLoader loader = new SvgLoader();
            Group svgImage = loader.loadSvg(svgFile);

            // Scale the image and wrap it in a Group to make the button
            // properly scale to the size of the image
            svgImage.setScaleX(0.1);
            svgImage.setScaleY(0.1);

            Group graphic = new Group(svgImage);

            // create a button and set the graphics node
            Button button = new Button();

            button.setPrefSize(270.0, 170.0);
            button.setMinSize(270.0, 170.0);
            graphic.scaleXProperty().bind(button.widthProperty().divide(70));
            graphic.scaleYProperty().bind(button.heightProperty().divide(70));
            button.setGraphic(graphic);
            button.setContentDisplay(ContentDisplay.BOTTOM);
            button.setUserData(className);
            VBox vBox = new VBox();
            Text text = new Text(className.substring(currentIndexPackage));
            vBox.getChildren().add(button);
            vBox.getChildren().add(text);
            vBox.setAlignment(Pos.CENTER);
            classFlowPane.getChildren().add(vBox);
            if (!apByClasses.containsKey(className)) {
                vBox.setVisible(false);
                vBox.setDisable(true);
            }

            button.setOnAction(event -> showClassDetails(button.getUserData().toString(),currentCommitVersions.get(0).getApByClasses().get(button.getUserData().toString())));

        }
    }

    private void showClassDetails(String s,List<AntiPatternInstance> apByClasses) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        VBox vBox = new VBox();
        Button exitButton = new Button();
        exitButton.setText("X");
        exitButton.setPrefHeight(30);
        exitButton.getStyleClass().add("closebutton");
        exitButton.setOnAction(event -> stackPane.getChildren().remove(scrollPane));
        vBox.getChildren().add(exitButton);
        VBox vBoxChild = new VBox();
        for (int i = 0; i < apByClasses.size() ; i++) {
            if (i > 0 && !apByClasses.get(i).getApName().equals(apByClasses.get(i-1).getApName())){
                TitledPane titledPane = new TitledPane();
                titledPane.setContent(vBoxChild);
                titledPane.setText(apByClasses.get(i-1).getApName());
                vBox.getChildren().add(titledPane);
                vBoxChild = new VBox();
            }
            vBoxChild.getChildren().add(new Text(apByClasses.get(i).getLocation().toString()));
        }
        TitledPane titledPane = new TitledPane();
        titledPane.setContent(vBoxChild);
        titledPane.setText(apByClasses.get(apByClasses.size()-1).getApName());
        vBox.getChildren().add(titledPane);
        scrollPane.setContent(vBox);
        stackPane.getChildren().add(scrollPane);

    }

    private int lastIndexOfSameSubSeq(String s, String s1) {
        int res = 0;
        for (; res < s.length(); res++) {
            if (s.charAt(res) != s1.charAt(res))
                return res;
        }
        return res;
    }


    private void setProjectChoiceDataSet() {
        List<CommitVersion> tmpList = new LinkedList<>(commitVersions);
        ObservableList data = FXCollections.observableArrayList();
        while (!tmpList.isEmpty()) {
            String projectName = tmpList.get(0).getName().replace("-" + tmpList.get(0).getCommit(), "");
            data.add(projectName);
            tmpList.removeIf(s -> s.getName().contains(projectName));
        }
        projectChoice.setItems(data);
        projectChoice.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, number2)
                -> handleProjectChange(String.valueOf(projectChoice.getItems().get((Integer) number2))));
        projectChoice.setValue(data.get(0));

    }

    private void handleProjectChange(String newProjectName) {
        currentIndexPackage = 0;
        for (CommitVersion commitVersion : commitVersions) {
            if (commitVersion.getName().contains(newProjectName)) {
                currentCommitVersions.add(commitVersion);
            }
        }
        commitSlider.setMin(0);
        commitSlider.setMax(currentCommitVersions.size() - 1);
        commitSlider.setValue(0);
        commitSlider.setMinorTickCount(1);
        commitSlider.setMajorTickUnit(1);
        commitSlider.setSnapToTicks(true);
        commitSlider.setShowTickMarks(true);
        commitSlider.setShowTickLabels(true);

        commitSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double n) {
                return currentCommitVersions.get(n.intValue()).getCommit();
            }

            @Override
            public Double fromString(String s) {
                for (int i = 0; i < currentCommitVersions.size(); i++) {
                    if (currentCommitVersions.get(i).getCommit().equals(s)) {
                        return (double) i;
                    }
                }
                return 0.0;
            }
        });
    }


}
