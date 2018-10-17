package controllers;

import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Objects;

public class PictureViewController {

    public Pane pane;
    public ImageView image;
    public ProgressIndicator progress;

    private Stage thisStage;

    public void initialize() {
        image.fitWidthProperty().bind(pane.widthProperty());
        image.fitHeightProperty().bind(pane.heightProperty());
    }

    public void Init(Stage stage) {
        thisStage = stage;
    }

    public void tryToShowPic(String fullPath) {
        var ext = fullPath.substring(fullPath.lastIndexOf(".")+1);
        if (Objects.equals(ext, "jpg") ||
                Objects.equals(ext, "jpeg") ||
                Objects.equals(ext, "png") ||
                Objects.equals(ext, "gif")) {
            image.setImage(new Image(String.format("file:%s", fullPath)));
        } else {
            progress.setVisible(true);
        }
    }
}
