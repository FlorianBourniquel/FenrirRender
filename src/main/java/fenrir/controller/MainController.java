package fenrir.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import fenrir.model.CommitVersion;

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
        researcherMode.setOnAction(event -> createNewWindows("/fxml/researcher.fxml"));
        architectMode.setOnAction(event -> createNewWindows("/fxml/architect.fxml"));
        developerMode.setOnAction(event -> createNewWindows("/fxml/developer.fxml"));
    }

    private void createNewWindows(String path) {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));

        Initializable controller;
        if (path.contains("researcher"))
            controller = new ResearcherController(commitVersions);
        else if (path.contains("architect"))
            controller = new ArchitectController(commitVersions);
        else
            controller = new DeveloperController(commitVersions);

        // Set it in the FXMLLoader
        loader.setController(controller);
        try {
            Parent root = loader.load();
            stage.setTitle("Graph");
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            if (path.contains("architect") || path.contains("developer"))
                scene.getStylesheets().add("./StyleSheetArchitectView.css");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
