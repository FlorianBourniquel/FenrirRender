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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import sample.model.AntiPatternInstance;
import sample.model.CommitVersion;
import sample.utils.SVGRange;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class ArchitectController implements Initializable {

    protected List<CommitVersion> commitVersions;
    protected List<String> classNames = new LinkedList<>();
    protected List<CommitVersion> currentCommitVersions = new LinkedList<>();
    protected List<SVGRange> svgRanges = new LinkedList<>();
    protected int currentIndexPackage = 0;

    @FXML
    private ChoiceBox projectChoice;

    @FXML
    private Slider commitSlider;

    @FXML
    private FlowPane classFlowPane;

    @FXML
    private HBox apHBox;

    @FXML
    private StackPane stackPane;
    protected int index;

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

    protected void setSVGRanges() {
        svgRanges.add(new SVGRange(0, 0, 0, "../../building/building-0-0.svg"));
        svgRanges.add(new SVGRange(1, 5, 0, "../../building/building-1-5.svg"));
        svgRanges.add(new SVGRange(6, 10, 0, "../../building/building-6-10.svg"));
        svgRanges.add(new SVGRange(11, 15, 0, "../../building/building-11-15.svg"));
        svgRanges.add(new SVGRange(16, 20, 0, "../../building/building-16-20.svg"));
        svgRanges.add(new SVGRange(21, 25, 0, "../../building/building-21-25.svg"));
        svgRanges.add(new SVGRange(26, 30, 0, "../../building/building-26-30.svg"));
        svgRanges.add(new SVGRange(31, 35, 0, "../../building/building-31-35.svg"));
        svgRanges.add(new SVGRange(36, Integer.MAX_VALUE, 0, "../../building/building-36-40.svg"));
        svgRanges.add(new SVGRange(1, 5, 1, "../../building/building-1-5-fire.svg"));
        svgRanges.add(new SVGRange(6, 10, 1, "../../building/building-6-10-fire.svg"));
        svgRanges.add(new SVGRange(11, 15, 1, "../../building/building-11-15-fire.svg"));
        svgRanges.add(new SVGRange(16, 20, 1, "../../building/building-16-20-fire.svg"));
        svgRanges.add(new SVGRange(21, 26, 1, "../../building/building-21-25-fire.svg"));
        svgRanges.add(new SVGRange(26, 30, 1, "../../building/building-26-30-fire.svg"));
        svgRanges.add(new SVGRange(31, 35, 1, "../../building/building-31-35-fire.svg"));
        svgRanges.add(new SVGRange(36, Integer.MAX_VALUE, 1, "../../building/building-36-40-fire.svg"));
        svgRanges.add(new SVGRange(0, 0, -1, "../../building/building-0-0-heal.svg"));
        svgRanges.add(new SVGRange(1, 5, -1, "../../building/building-1-5-heal.svg"));
        svgRanges.add(new SVGRange(6, 10, -1, "../../building/building-6-10-heal.svg"));
        svgRanges.add(new SVGRange(11, 15, -1, "../../building/building-11-15-heal.svg"));
        svgRanges.add(new SVGRange(16, 20, -1, "../../building/building-16-20-heal.svg"));
        svgRanges.add(new SVGRange(21, 26, -1, "../../building/building-21-25-heal.svg"));
        svgRanges.add(new SVGRange(26, 30, -1, "../../building/building-26-30-heal.svg"));
        svgRanges.add(new SVGRange(31, 35, -1, "../../building/building-31-35-heal.svg"));
        svgRanges.add(new SVGRange(36, Integer.MAX_VALUE, -1, "../../building/building-36-40-heal.svg"));
    }

    protected void setClassFlowPane() {
        index = (int) commitSlider.getValue();
        Map<String, List<AntiPatternInstance>> apByClasses = currentCommitVersions.get(index).getApByClasses();
        if (currentIndexPackage == 0) {
            Object[] strings = apByClasses.keySet().toArray();
            if (strings.length > 2)
                currentIndexPackage = lastIndexOfSameSubSeq(strings);
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
                } else if (!currentCommitVersions.get(index - 1).getApByClasses().containsKey(className))
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

            button.setOnAction(event -> showClassDetails(button.getUserData().toString(), currentCommitVersions.get(index).getApByClasses().get(button.getUserData().toString())));

            setAPHboxDataSet();

        }
    }

    protected void setAPHboxDataSet() {
        apHBox.getChildren().clear();
        for (String ap : currentCommitVersions.get(index).getAntiPatterns().keySet()) {
            RadioButton radioButton = new RadioButton();
            radioButton.setText(ap);
            HBox.setHgrow(radioButton, Priority.ALWAYS);
            radioButton.setAlignment(Pos.CENTER);
            radioButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            radioButton.setSelected(false);
            radioButton.selectedProperty().addListener((obs, wasPreviouslySelected, isNowSelected) -> {
                if (isNowSelected) {
                    addAPUnderline(radioButton.getText());
                } else {
                    removeAPUnderline(radioButton.getText());
                }
            });
            apHBox.getChildren().add(radioButton);
        }
    }

    protected void removeAPUnderline(String text) {
        List<String> apSelected = new LinkedList<>();
        for (Node node : apHBox.getChildren()) {
            RadioButton radioButton = (RadioButton) node;
            if (radioButton.isSelected())
                apSelected.add(radioButton.getText());
        }
        myLabel:
        for (Node node : classFlowPane.getChildren()) {
            VBox vBox = (VBox) node;
            List<AntiPatternInstance> antiPatternInstanceList = currentCommitVersions.get(index).getApByClasses().get(vBox.getChildren().get(0).getUserData().toString());
            if (antiPatternInstanceList != null && antiPatternInstanceList.stream().anyMatch(ap -> ap.getApName().equals(text))) {
                for (String s : apSelected) {
                    if (antiPatternInstanceList.stream().anyMatch(ap -> ap.getApName().equals(s)))
                        continue myLabel;
                }
                Button button = (Button) vBox.getChildren().get(0);
                button.setStyle("-fx-border-color: transparent;");
            }
        }
    }

    protected void addAPUnderline(String text) {
        for (Node node : classFlowPane.getChildren()) {
            VBox vBox = (VBox) node;
            List<AntiPatternInstance> antiPatternInstanceList = currentCommitVersions.get(index).getApByClasses().get(vBox.getChildren().get(0).getUserData().toString());
            if (antiPatternInstanceList != null && antiPatternInstanceList.stream().anyMatch(ap -> ap.getApName().equals(text))) {
                Button button = (Button) vBox.getChildren().get(0);
                button.setStyle("-fx-border-width: 5 5 5 5; -fx-border-color: purple;");
            }
        }
    }

    protected void showClassDetails(String className, List<AntiPatternInstance> apList) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        VBox vBox = new VBox();
        Button exitButton = new Button();
        exitButton.setText("X");
        exitButton.setPrefHeight(30);
        exitButton.getStyleClass().add("closebutton");
        exitButton.setOnAction(event -> stackPane.getChildren().remove(scrollPane));

        VBox vBoxChild = new VBox();
        for (int i = 0; i < apList.size(); i++) {
            if (i > 0 && !apList.get(i).getApName().equals(apList.get(i - 1).getApName())) {
                TitledPane titledPane = new TitledPane();
                titledPane.setContent(vBoxChild);
                titledPane.setText(apList.get(i - 1).getApName());
                vBox.getChildren().add(titledPane);
                vBoxChild = new VBox();
            }
            HBox hBox = new HBox();
            Text text = new Text(apList.get(i).getLocation().toString());
            hBox.getChildren().add(text);
            hBox.setSpacing(5.0);
            ImageView imageView = null;
            if (index > 0) {
                if (currentCommitVersions.get(index - 1).getApByClasses().containsKey(className)) {
                    if (!currentCommitVersions.get(index - 1).getApByClasses().get(className).contains(apList.get(i)))
                        imageView = new ImageView(new Image(getClass().getResourceAsStream("../../status/fire.png")));
                } else
                    imageView = new ImageView(new Image(getClass().getResourceAsStream("../../status/fire.png")));
            }

            if (imageView != null) {
                imageView.setFitHeight(17);
                imageView.setFitWidth(17);
                imageView.setPreserveRatio(true);
                hBox.getChildren().add(imageView);
            }
            vBoxChild.getChildren().add(hBox);
            vBoxChild.setSpacing(5.0);
        }

        if (!vBoxChild.getChildren().isEmpty()) {
            TitledPane titledPane = new TitledPane();
            titledPane.setContent(vBoxChild);
            titledPane.setText(apList.get(apList.size() - 1).getApName());
            vBox.getChildren().add(titledPane);
        }

        if (index > 0 && currentCommitVersions.get(index - 1).getApByClasses().containsKey(className)) {
            myLabel:
            for (AntiPatternInstance antiPatternInstance : currentCommitVersions.get(index - 1).getApByClasses().get(className)) {
                if (!apList.contains(antiPatternInstance)) {
                    HBox hBox = new HBox();
                    Text text = new Text(antiPatternInstance.getLocation().toString());
                    hBox.getChildren().add(text);
                    hBox.setSpacing(5.0);
                    ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("../../status/heal.png")));
                    imageView.setFitHeight(17);
                    imageView.setFitWidth(17);
                    imageView.setPreserveRatio(true);
                    hBox.getChildren().add(imageView);
                    for (int i = 0; i < vBox.getChildren().size(); i++) {
                        TitledPane titledPane = (TitledPane) vBox.getChildren().get(i);
                        if (titledPane.getText().equals(antiPatternInstance.getApName())) {
                            ((VBox) titledPane.getContent()).getChildren().add(hBox);
                            continue myLabel;
                        }

                    }
                    TitledPane titledPane = new TitledPane();
                    vBoxChild = new VBox();
                    vBoxChild.getChildren().add(hBox);
                    titledPane.setContent(vBoxChild);
                    titledPane.setText(antiPatternInstance.getApName());
                    vBox.getChildren().add(titledPane);
                }
            }
        }
        ObservableList<Node> data = FXCollections.observableArrayList(vBox.getChildren());
        data.sort(Comparator.comparing(t -> ((TitledPane) t).getText()));

        data.add(0, exitButton);
        vBox.getChildren().clear();
        vBox.getChildren().addAll(data);
        scrollPane.setContent(vBox);
        stackPane.getChildren().add(scrollPane);

    }

    protected int lastIndexOfSameSubSeq(Object[] strings) {
        String s = strings[0].toString();
        String s1 = strings[1].toString();
        for (int i = 1; i < strings.length; i++) {
            if (s.lastIndexOf('.') != strings[i].toString().lastIndexOf('.')) {
                s1 = strings[i].toString();
                break;
            }
        }
        int res = 0;
        for (; res < s.length(); res++) {
            if (s.charAt(res) != s1.charAt(res))
                return res;
        }
        return res;
    }


    protected void setProjectChoiceDataSet() {
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

    protected void handleProjectChange(String newProjectName) {
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
