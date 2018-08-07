package fenrir.controller;

import afester.javafx.svg.SvgLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import fenrir.model.AntiPatternInstance;
import fenrir.model.CommitVersion;

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
    private Map<String, List<AntiPatternInstance>> apByClasses1 = new HashMap<>();
    private Map<String, List<AntiPatternInstance>> apByClasses2 = new HashMap<>();

    public DeveloperController(List<CommitVersion> commitVersions) {
        super(commitVersions);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commitVersions.sort(Comparator.comparing(CommitVersion::getDate).reversed());
        setSVGRanges();
        setProjectChoiceDataSet();
        setClassFlowPane(choiceBoxFirstCommit.getValue().toString(),choiceBoxSecondCommit.getValue().toString());
    }


    private void setClassFlowPane(String commit1, String commit2) {
        apList.clear();
        apMapByClass.clear();
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
                apMapByClass.put(entry.getKey(),apByClasses2.get(entry.getKey()));
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

            int size = apMapByClass.get(className).size();
            if (!apByClasses1.containsKey(className))
                size = 0;
            else if (!apByClasses2.containsKey(className))
                size = apMapByClass.get(className).size();
            else {
                for (AntiPatternInstance antiPatternInstance : apMapByClass.get(className)) {
                    if (!apByClasses1.get(className).contains(antiPatternInstance))
                        size--;
                }
            }
            final int finalSize = size;
            InputStream svgFile = getClass()
                    .getResourceAsStream(svgRanges.stream()
                            .filter(svgRange -> svgRange.isFireHealOrNeutral() == finalIsFireHealOrNeutral
                                    && svgRange.getMax() >= finalSize
                                    && svgRange.getMin() <= finalSize)
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

    @Override
    protected void setAPHboxDataSet() {
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
                        addAPUnderline(radioButton.getText(),classFlowPane,apMapByClass);
                    } else {
                        removeAPUnderline(radioButton.getText(),classFlowPane,apMapByClass);
                    }
            });
            apHBox.getChildren().add(radioButton);
        }
    }

    @Override
    protected void handleProjectChange(String newProjectName) {
        currentIndexPackage = 0;
        ObservableList<String> data = FXCollections.observableArrayList();
        currentCommitVersions.clear();
        for (CommitVersion commitVersion : commitVersions) {
            if (commitVersion.getName().contains(newProjectName)) {
                currentCommitVersions.add(commitVersion);
                data.add(commitVersion.getCommit());
            }
        }
        choiceBoxFirstCommit.setItems(data);
        choiceBoxFirstCommit.setValue(data.get(0));
        ObservableList<String> data2 = FXCollections.observableArrayList(data);

        if (data2.size() > 1) {
            data2.remove(0);
            choiceBoxSecondCommit.setItems(data2);
            choiceBoxSecondCommit.setItems(data2);
            choiceBoxSecondCommit.setValue(data2.get(0));
            data.remove(data2.get(0));
        }
        else {
            choiceBoxSecondCommit.setItems(data);
            choiceBoxSecondCommit.setValue(data.get(0));
        }
        choiceBoxFirstCommit.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, number2) -> {
            if (updating) {
                updating = false;
                if (choiceBoxSecondCommit.getItems().size() > 1) {
                choiceBoxSecondCommit.getItems().add(choiceBoxFirstCommit.getItems().get(number.intValue()).toString());
                choiceBoxSecondCommit.getItems().remove(choiceBoxFirstCommit.getItems().get(number2.intValue()).toString());
                }
                updating = true;
                if (number2.intValue() > -1)
                    setClassFlowPane(choiceBoxFirstCommit.getItems().get(number2.intValue()).toString(), choiceBoxSecondCommit.getValue().toString());
                else
                    setClassFlowPane(choiceBoxFirstCommit.getItems().get(number.intValue()).toString(), choiceBoxSecondCommit.getValue().toString());
            }

        });
        choiceBoxSecondCommit.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, number2) -> {
            if (updating) {
                updating = false;
                if (choiceBoxFirstCommit .getItems().size() > 1) {
                    choiceBoxFirstCommit.getItems().add(choiceBoxSecondCommit.getItems().get(number.intValue()).toString());
                    choiceBoxFirstCommit.getItems().remove(choiceBoxSecondCommit.getItems().get(number2.intValue()).toString());
                }
                updating = true;
                if (number2.intValue() > -1)
                    setClassFlowPane(choiceBoxFirstCommit.getValue().toString(), choiceBoxSecondCommit.getItems().get(number2.intValue()).toString());
                else
                    setClassFlowPane(choiceBoxFirstCommit.getValue().toString(), choiceBoxSecondCommit.getItems().get(number.intValue()).toString());
            }

        });

    }

    @Override
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

            if (apByClasses2.containsKey(className)) {
                if (!apByClasses2.get(className).contains(apList.get(i)))
                    imageView = new ImageView(new Image(getClass().getResourceAsStream("/status/fire.png")));
                else if (apByClasses2.get(className).contains(apList.get(i)) && !apByClasses1.get(className).contains(apList.get(i)))
                    imageView = new ImageView(new Image(getClass().getResourceAsStream("/status/heal.png")));
            } else
                imageView = new ImageView(new Image(getClass().getResourceAsStream("/status/fire.png")));


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

        vBox.getChildren().add(0,exitButton);
        scrollPane.setContent(vBox);
        stackPane.getChildren().add(scrollPane);

    }


}
