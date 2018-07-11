package sample.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;
import sample.model.CommitVersion;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private Button developerMode;

    @FXML
    private Button architectMode;

    @FXML
    private Button researcherMode;

    private List<CommitVersion> commitVersions;

    public MainController(List<CommitVersion> commitVersions) {
        this.commitVersions = commitVersions;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        researcherMode.setOnAction(event -> createNewWindows("../view/researcher.fxml"));
    }

    private void createNewWindows(String path) {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));

        // Create a researcherController instance
        ResearcherController researcherController = new ResearcherController(commitVersions);
        // Set it in the FXMLLoader
        loader.setController(researcherController);
        try {
            Parent root = loader.load();
            stage.setTitle("Graph");
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
