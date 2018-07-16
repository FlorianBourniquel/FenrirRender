package sample.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.util.StringConverter;
import sample.model.AntiPatternInstance;
import sample.model.CommitVersion;

import java.net.URL;
import java.util.*;

public class ArchitectController implements Initializable {

    private List<CommitVersion> commitVersions;
    private List<CommitVersion> currentCommitVersions = new LinkedList<>();
    private int currentIndexPackage = 0;

    @FXML
    private ChoiceBox projectChoice;

    @FXML
    private Slider commitSlider;

    @FXML
    private FlowPane classFlowPane;

    public ArchitectController(List<CommitVersion> commitVersions) {
        this.commitVersions = commitVersions;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commitVersions.sort(Comparator.comparing(CommitVersion::getDate));
        setProjectChoiceDataSet();
        setClassFlowPane();
    }

    private void setClassFlowPane() {
        Map<String, List<AntiPatternInstance>> map = currentCommitVersions.get(0).getApByClasses();
        if (currentIndexPackage == 0) {
            Object[] strings = map.keySet().toArray();
            if (strings.length > 2)
                currentIndexPackage = lastIndexOfSameSubSeq(strings[0].toString(),strings[1].toString());
            System.out.println(strings[0].toString().substring(currentIndexPackage));
        }
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
        for (CommitVersion commitVersion:commitVersions) {
            if (commitVersion.getName().contains(newProjectName)) {
                currentCommitVersions.add(commitVersion);
            }
        }
        commitSlider.setMin(0);
        commitSlider.setMax(currentCommitVersions.size() - 1);
        commitSlider.setValue(0);
        commitSlider.setMinorTickCount(0);
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
