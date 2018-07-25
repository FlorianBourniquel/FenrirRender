package sample.controller;

import afester.javafx.svg.SvgLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import sample.model.AntiPatternInstance;
import sample.model.CommitVersion;
import sample.model.PairAPDataLocation;
import sample.model.PairAPName;
import sample.utils.SVGRange;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class DeveloperController extends ArchitectController implements Initializable {

    private Map<String, List<AntiPatternInstance>> apMapByClass = new HashMap<>();
    private List<String> apList = new LinkedList<>();


    @FXML
    private ChoiceBox projectChoice;

    @FXML
    private ChoiceBox choiceBoxSecondCommit;

    @FXML
    private ChoiceBox choiceBoxFirstCommit;

    @FXML
    private Slider commitSlider;

    @FXML
    private FlowPane classFlowPane;

    @FXML
    private HBox apHBox;

    @FXML
    private StackPane stackPane;

    private boolean updating = true;

    public DeveloperController(List<CommitVersion> commitVersions) {
        super(commitVersions);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setSVGRanges();
        setProjectChoiceDataSet();
        setClassFlowPane(choiceBoxFirstCommit.getValue().toString(),choiceBoxSecondCommit.getValue().toString());
    }


    private void setClassFlowPane(String commit1, String commit2) {
        apList.clear();
        apMapByClass.clear();
        Map<String, List<AntiPatternInstance>> apByClasses1 = new HashMap<>();
        Map<String, List<AntiPatternInstance>> apByClasses2 = new HashMap<>();
        for (CommitVersion commitVersion:currentCommitVersions) {
            if (commitVersion.getCommit().equals(commit1)) {
                apByClasses1 = commitVersion.getApByClasses();
                for (String ap :commitVersion.getAntiPatterns().keySet()) {
                    if (!apList.contains(ap))
                        apList.add(ap);
                }
            }
            else if (commitVersion.getCommit().equals(commit2)) {
                apByClasses2 = commitVersion.getApByClasses();
                for (String ap :commitVersion.getAntiPatterns().keySet()) {
                    if (!apList.contains(ap))
                        apList.add(ap);
                }
            }
        }
        for (Map.Entry<String, List<AntiPatternInstance>> entry : apByClasses1.entrySet()) {
            if (!apByClasses2.containsKey(entry.getKey())) {
                apMapByClass.put(entry.getKey(),entry.getValue());
            }
            else if (!entry.getValue().equals(apByClasses2)) {
                List<AntiPatternInstance> antiPatternInstanceList = new LinkedList<>();
                antiPatternInstanceList.addAll(entry.getValue());
                for (AntiPatternInstance antiPatternInstance: apByClasses2.get(entry.getKey())) {
                    if (!antiPatternInstanceList.contains(antiPatternInstance))
                        antiPatternInstanceList.add(antiPatternInstance);
                }
                apMapByClass.put(entry.getKey(),antiPatternInstanceList);
            }
        }
        for (Map.Entry<String, List<AntiPatternInstance>> entry : apByClasses2.entrySet()) {
            if (!apMapByClass.containsKey(entry.getKey())) {
                apMapByClass.put(entry.getKey(),new LinkedList<>());
            }
        }

        Object[] strings = apMapByClass.keySet().toArray();
        if (strings.length > 2)
            currentIndexPackage = lastIndexOfSameSubSeq(strings);


        classFlowPane.getChildren().clear();
        for (String className : apMapByClass.keySet()) {
            int isFireHealOrNeutral = 1;
            if (apByClasses2.containsKey(className) && apByClasses1.containsKey(className)) {
                isFireHealOrNeutral = Integer.compare(apByClasses1.get(className).size(), apByClasses2.get(className).size());
            }
            else if (apByClasses2.containsKey(className)) {
                isFireHealOrNeutral = -1;
            }
            final int finalIsFireHealOrNeutral = isFireHealOrNeutral;

            InputStream svgFile = getClass()
                    .getResourceAsStream(svgRanges.stream()
                            .filter(svgRange -> apMapByClass.containsKey(className)
                                    && svgRange.isFireHealOrNeutral() == finalIsFireHealOrNeutral
                                    && svgRange.getMax() >= apMapByClass.get(className).size()
                                    && svgRange.getMin() <= apMapByClass.get(className).size())
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
            if (!apMapByClass.containsKey(className)) {
                vBox.setVisible(false);
                vBox.setDisable(true);
            }

            button.setOnAction(event -> showClassDetails(button.getUserData().toString(),apMapByClass.get(button.getUserData().toString())));

            setAPHboxDataSet();

        }
    }

    private void setAPHboxDataSet() {
        apHBox.getChildren().clear();
        for (String ap : apList) {
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

    private void removeAPUnderline(String text) {
        List<String> apSelected = new LinkedList<>();
        for (Node node : apHBox.getChildren()) {
            RadioButton radioButton = (RadioButton) node;
            if (radioButton.isSelected())
                apSelected.add(radioButton.getText());
        }
        myLabel: for (Node node : classFlowPane.getChildren()) {
            VBox vBox = (VBox) node;
            List<AntiPatternInstance> antiPatternInstanceList = apMapByClass.get(vBox.getChildren().get(0).getUserData().toString());
            if (antiPatternInstanceList != null && antiPatternInstanceList.stream().anyMatch(ap -> ap.getApName().equals(text))) {
                for (String s: apSelected) {
                    if (antiPatternInstanceList.stream().anyMatch(ap -> ap.getApName().equals(s)))
                        continue myLabel;
                }
                Button button = (Button) vBox.getChildren().get(0);
                button.setStyle("-fx-border-color: transparent;");
            }
        }
    }

    private void addAPUnderline(String text) {
        for (Node node : classFlowPane.getChildren()) {
            VBox vBox = (VBox) node;
            List<AntiPatternInstance> antiPatternInstanceList = apMapByClass.get(vBox.getChildren().get(0).getUserData().toString());
            if (antiPatternInstanceList != null && antiPatternInstanceList.stream().anyMatch(ap -> ap.getApName().equals(text))) {
                Button button = (Button) vBox.getChildren().get(0);
                button.setStyle("-fx-border-width: 5 5 5 5; -fx-border-color: purple;");
            }
        }
    }




    protected void handleProjectChange(String newProjectName) {
        currentIndexPackage = 0;
        ObservableList<String> data = FXCollections.observableArrayList();
        for (CommitVersion commitVersion : commitVersions) {
            if (commitVersion.getName().contains(newProjectName)) {
                currentCommitVersions.add(commitVersion);
                data.add(commitVersion.getCommit());
            }
        }
        choiceBoxFirstCommit.setItems(data);
        choiceBoxFirstCommit.setValue(data.get(0));
        ObservableList<String> data2 = FXCollections.observableArrayList(data);
        data2.remove(0);
        choiceBoxSecondCommit.setItems(data2);
        if (data2.size() > 1) {
            choiceBoxSecondCommit.setItems(data2);
            choiceBoxSecondCommit.setValue(data2.get(0));
            data.remove(data2.get(0));
        }
        choiceBoxFirstCommit.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, number2) -> {
            if (updating) {
                updating = false;
                choiceBoxSecondCommit.getItems().add(choiceBoxFirstCommit.getItems().get(number.intValue()).toString());
                choiceBoxSecondCommit.getItems().remove(choiceBoxFirstCommit.getItems().get(number2.intValue()).toString());
                updating = true;
                setClassFlowPane(choiceBoxFirstCommit.getItems().get(number2.intValue()).toString(), choiceBoxSecondCommit.getValue().toString());
            }

        });
        choiceBoxSecondCommit.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, number2) -> {
            if (updating) {
                updating = false;
                choiceBoxFirstCommit.getItems().add(choiceBoxSecondCommit.getItems().get(number.intValue()).toString());
                choiceBoxFirstCommit.getItems().remove(choiceBoxSecondCommit.getItems().get(number2.intValue()).toString());
                updating = true;
                setClassFlowPane(choiceBoxFirstCommit.getValue().toString(), choiceBoxSecondCommit.getItems().get(number2.intValue()).toString());
            }

        });

    }


}