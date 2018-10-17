package controllers;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.FileModel;
import models.SettingsListener;
import tasks.SearchFilesTask;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HomeController implements SettingsListener {

    public VBox vBox;
    public TableView<FileModel> tableView;
    public TableColumn<FileModel, String> fileNameCol;
    public TableColumn<FileModel, String> fileExtCol;
    public TableColumn<FileModel, String> fileSizeCol;
    public TableColumn<FileModel, LocalDate> fileCreationTimeCol;
    public TableColumn<FileModel, LocalDate> fileLastModifiedCol;
    public Label labelStatus;
    public MenuItem menuItemStartSearch;
    public MenuItem menuItemSettings;
    public ProgressIndicator progressIndicator;

    private ObservableList<FileModel> fileData = FXCollections.observableArrayList();
    private String typeFile = null;
    private String pathSearch = null;
    private FileModel selectedModel;
    private Stage thisStage;
    private TextEditorController textEditorController = null;
    private Stage textEditorStage = null;

    /** Программная инициализация компонентов UI */
    public void initialize() {
        // Настройка TableView -----------------------------------------------------------------------------------------
        // Привязка свойств модели к таблице
        fileNameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        fileExtCol.setCellValueFactory(cellData -> cellData.getValue().extensionProperty());
        fileSizeCol.setCellValueFactory(cellData -> cellData.getValue().sizeProperty());
        fileCreationTimeCol.setCellValueFactory(cellData -> cellData.getValue().creationTimeProperty());
        fileLastModifiedCol.setCellValueFactory(cellData -> cellData.getValue().lastModifiedProperty());

        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tableView.setItems(fileData);

        // Кастомная отрисовка ячейки с датой
        fileCreationTimeCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    // Вывод форматированной даты
                    setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(item));
                } // if
            } // updateItem
        });
        fileLastModifiedCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    // Вывод форматированной даты
                    setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(item));
                } // if
            } // updateItem
        });

        // Настройка фабрики строки. Обработчик контекстного меню
        tableView.setRowFactory(table -> {
            final TableRow<FileModel> row = new TableRow<>();
            final ContextMenu rowMenu = new ContextMenu();

            var textEditItem = new MenuItem("Открыть в текстовом редакторе...");
            textEditItem.setOnAction(event -> {
                selectedModel = row.getItem();
                showTextEditor(selectedModel.getFullPath());
            });
            var pictureViewItem = new MenuItem("Просмотреть, как изображение...");
            pictureViewItem.setOnAction(event -> {
                selectedModel = row.getItem();
                showPictureView(selectedModel.getFullPath());
            });

            rowMenu.getItems().addAll(textEditItem, pictureViewItem);

            // Отображать контекстное меню только для ненулевых элементов:
            row.contextMenuProperty().bind(
                    Bindings.when(Bindings.isNotNull(row.itemProperty()))
                            .then(rowMenu)
                            .otherwise((ContextMenu)null));
            return row;
        });
    }

    /** Начальная инициализация при открытии окна */
    public void Init(Stage stage) {
        thisStage = stage;
        menuItemStartSearch.disableProperty().bind(progressIndicator.visibleProperty());
        menuItemSettings.disableProperty().bind(progressIndicator.visibleProperty());
    }

    /** Начать поиск */
    public void startSearch() {
        //       Если был выделен элемент в таблице и после этого запустили поиск
        // TODO: Exception in thread "JavaFX Application Thread" java.lang.NullPointerException
        if (typeFile == null || pathSearch == null) {
            showSettings();
        } else {
            tableView.getSelectionModel().select(null);
            labelStatus.setText(String.format("Идёт поиск %s файлов в %s", typeFile, pathSearch));
            progressIndicator.setVisible(true);
            var thread = new Thread(createSearchTask());
            thread.setDaemon(true);
            thread.start();
        } // if
    }

    /** Настройки поиска */
    public void showSettings() {
        try {
            var classLoader = getClass().getClassLoader();
            var fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("../views/Settings.fxml"));

            var stage = new Stage();
            stage.setScene(new Scene(fxmlLoader.load(), 500, 350));
            stage.setTitle("Настройки");
            stage.initOwner(vBox.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);

            var iconFile = new File(Objects.requireNonNull(classLoader.getResource("icons/settings.png")).getFile());
            stage.getIcons().add(new Image(String.format("file:%s", iconFile.getAbsolutePath())));

            SettingsController controller = fxmlLoader.getController();
            controller.Init(stage);
            controller.setSettingsListener(this);

            stage.show();
        } catch (IOException e) {
            var logger = Logger.getLogger(getClass().getName());
            logger.log(Level.SEVERE, "Ошибка при создании окна настроек!", e);
        }
    }

    /** Показать окно с редактором текста */
    public void showTextEditor(String path) {
        try {
            var classLoader = getClass().getClassLoader();
            var fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("../views/TextEditor.fxml"));

            if (textEditorStage == null) {
                textEditorStage = new Stage();
                textEditorStage.setScene(new Scene(fxmlLoader.load(), 700, 500));
                textEditorStage.setTitle("Текстовый редактор");
                textEditorStage.initModality(Modality.NONE);
                textEditorStage.setResizable(true);
                var iconFile = new File(Objects.requireNonNull(classLoader.getResource("icons/textEditor.png")).getFile());
                textEditorStage.getIcons().add(new Image(String.format("file:%s", iconFile.getAbsolutePath())));
            } // if

            if (textEditorController == null) {
                textEditorController = fxmlLoader.getController();
                textEditorController.Init(textEditorStage);
                textEditorStage.setIconified(false);
                textEditorStage.requestFocus();
                textEditorStage.toFront();
                textEditorStage.show();
                textEditorController.openFileNoChooser(new File(path));
            } else {
                textEditorStage.setIconified(false);
                textEditorStage.requestFocus();
                textEditorStage.toFront();
                textEditorStage.show();
                textEditorController.openFileNoChooser(new File(path));
            } // if
        } catch (IOException e) {
            var logger = Logger.getLogger(getClass().getName());
            logger.log(Level.SEVERE, "Ошибка при создании окна текстового редактора!", e);
        }
    }

    /** Показать окно отображение картинки */
    private void showPictureView(String fullPath) {
        try {
            var classLoader = getClass().getClassLoader();
            var fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("../views/PictureView.fxml"));

            var stage = new Stage();
            stage.setScene(new Scene(fxmlLoader.load(), 700, 500));
            stage.setTitle(fullPath);
            stage.initModality(Modality.NONE);
            stage.setResizable(true);

            var iconFile = new File(Objects.requireNonNull(classLoader.getResource("icons/pictureView.png")).getFile());
            stage.getIcons().add(new Image(String.format("file:%s", iconFile.getAbsolutePath())));

            PictureViewController controller = fxmlLoader.getController();
            controller.Init(stage);
            controller.tryToShowPic(fullPath);

            stage.show();
        } catch (IOException e) {
            var logger = Logger.getLogger(getClass().getName());
            logger.log(Level.SEVERE, "Ошибка при создании окна текстового редактора!", e);
        }
    }

    /** Выход */
    public void exit() {
        System.exit(0);
    }

    /** О программе */
    public void about() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("О программе");
        alert.setHeaderText("Поисковик различного типа файлов \nс возможностью быстрого просмотра \nизображений и редактирования текста.");
        alert.setContentText("Написанный с помощью технологии Java FX.\n\nCopyright (c) Алексей Юрьевич Бурьянов, 2018.");
        alert.showAndWait();
    }

    /** Создаёт задачу для поиска по заданому пути и маске */
    private SearchFilesTask createSearchTask() {
        var task = new SearchFilesTask(typeFile, pathSearch);
        task.setOnSucceeded(event -> {
            try {
                labelStatus.setText("Поиск завершён");
                progressIndicator.setVisible(false);
                fileData.clear();
                fileData.addAll(task.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } // try-catch
        });
        task.setOnFailed(event -> {
            progressIndicator.setVisible(false);
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Поиск завершён с ошибкой!");
            alert.setHeaderText("Что-то пошло не так...");
            alert.showAndWait();
        });
        return task;
    }

    /** Callback-вызов при применении настроек. Запускается поиск */
    @Override
    public void sendSettings(String typeFile, String pathSearch) {
        this.typeFile = typeFile;
        this.pathSearch = pathSearch;
        labelStatus.setText(String.format("Идёт поиск %s файлов в %s", typeFile, pathSearch));
        progressIndicator.setVisible(true);
        var thread = new Thread(createSearchTask());
        thread.setDaemon(true);
        thread.start();
    }
}
