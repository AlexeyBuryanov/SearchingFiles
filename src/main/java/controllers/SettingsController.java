package controllers;

import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.SettingsListener;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class SettingsController {

    public VBox vBox;
    public TextField textBoxTypeFile;
    public TextField textBoxPathSearch;

    private Stage currentStage;
    private String typeFile;
    private String pathSearch;
    private String pathSettings = System.getProperty("user.dir")+"\\src\\main\\resources\\settings.json";
    private SettingsListener settingsListener;

    public SettingsListener getSettingsListener() { return settingsListener; }
    public void setSettingsListener(SettingsListener settingsListener) { this.settingsListener = settingsListener; }

    /** Начальная инициализация при открытии окна */
    public void Init(Stage stage) {
        currentStage = stage;
        readSettings();
    }

    /** Выполняет чтение и парсинг json-файла с настройками */
    private void readSettings() {
        try {
            var parser = new JSONParser();
            var obj = parser.parse(new FileReader(pathSettings));
            var jsonObj = (JSONObject) obj;
            typeFile = (String)jsonObj.get("typeFile");
            pathSearch = (String)jsonObj.get("pathSearch");
            textBoxTypeFile.setText(typeFile);
            textBoxPathSearch.setText(pathSearch);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Ошибка чтения");
            alert.setHeaderText("Невозможно прочитать файл настроек");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    /** Ок */
    public void onOkClick() {
        var root = new JSONObject();
        root.put("groupId", "com.alexeyburyanov.searchingfiles");
        root.put("artifactId", "SearchingFiles");
        root.put("version", "1.0-SNAPSHOT");
        root.put("typeFile", textBoxTypeFile.getText());
        root.put("pathSearch", textBoxPathSearch.getText());
        writeFile(pathSettings, root.toJSONString());
        // Отправляем новые настройки в главное окно используя callback-вызов
        if (settingsListener != null) {
            if (textBoxTypeFile.getText() != null &&
                    !Objects.equals(textBoxTypeFile.getText(), "") &&
                    textBoxPathSearch.getText() != null &&
                    !Objects.equals(textBoxPathSearch.getText(), "")) {
                settingsListener.sendSettings(textBoxTypeFile.getText(), textBoxPathSearch.getText());
                currentStage.close();
            } else {
                var alert = new Alert(Alert.AlertType.WARNING);
                alert.initStyle(StageStyle.UTILITY);
                alert.setTitle("Заполните поля");
                alert.setHeaderText("Пожалуйста, не оставляйте поля пустыми!");
                alert.showAndWait();
            } // if
        } // if
    }

    /** Отмена */
    public void onCancelClick() {
        currentStage.close();
    }

    /** Диалог для выбора папки */
    public void overviewDialog() {
        // Настройка диалога
        var directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выберите папку в которой хотите произвести поиск");
        directoryChooser.setInitialDirectory(new File(pathSearch));
        var selectedDirectory = directoryChooser.showDialog(vBox.getScene().getWindow());
        if (selectedDirectory != null) {
            pathSearch = selectedDirectory.getAbsolutePath();
            textBoxPathSearch.setText(pathSearch);
        } // if
    }

    /** Запись файла по указанному пути */
    private void writeFile(String fileName, String json) {
        try (var writer = new FileWriter(fileName, false)) {
            writer.write(json);
            writer.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Ошибка записи");
            alert.setHeaderText("Невозможно записать данный файл");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }
}