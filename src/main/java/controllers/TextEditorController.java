package controllers;

import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TextEditorController {

    public VBox VBoxMain;
    public TabPane TabPaneMain;
    public Label LabelStatus;

    // Вкладки
    private List<Tab> _tabList = new LinkedList<>();
    // Панели внутри вкладок
    private List<AnchorPane> _anchorPaneList = new LinkedList<>();
    // Контент панели внутри вкладок
    private List<TextArea> _textAreaList = new LinkedList<>();
    // Пути для сохранения
    private List<String> _pathsSave = new LinkedList<>();
    private Stage thisStage;

    public void initialize() {

    }

    public void Init(Stage stage) {
        thisStage = stage;
    }

    /** Новый документ */
    public void createNewDoc() {
        // Создаём новую вкладку содержащую в себе AnchorPane с
        // текстовой зоной на всё доступное пространство панели
        var newTab = new Tab("Новый");
        var newTextArea = new TextArea();
        AnchorPane.setTopAnchor(newTextArea, 0.0);
        AnchorPane.setRightAnchor(newTextArea, 0.0);
        AnchorPane.setLeftAnchor(newTextArea, 0.0);
        AnchorPane.setBottomAnchor(newTextArea, 0.0);
        var newPane = new AnchorPane(newTextArea);
        newTab.setContent(newPane);
        // Добавляем вкладку в панель вкладок
        TabPaneMain.getTabs().add(newTab);
        // Заполняем списки всех созданных компонентов
        // для дальнейшего обращения к ним по индексу
        _tabList.add(newTab);
        _anchorPaneList.add(newPane);
        _textAreaList.add(newTextArea);
        _pathsSave.add("");
        // Выделяем последний таб-элемент
        TabPaneMain.getSelectionModel().selectLast();
    }

    /** Открытие файла */
    public void openFile() {
        // Настройка диалога
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл, который хотите прочесть, как текстовый");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Все файлы", "*.*"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));
        // Показываем диалог
        File selectedFile = fileChooser.showOpenDialog(VBoxMain.getScene().getWindow());
        // Если диалог подтверждён
        if (selectedFile != null)  {
            // Создаём новый документ
            createNewDoc();
            // Получаем путь
            String path = selectedFile.getAbsolutePath();
            // Сохраняем путь по выбранному индексу
            _pathsSave.set(TabPaneMain.getSelectionModel().getSelectedIndex(), path);
            // Читаем файл
            readFile(path);
            // Меняем тайтл вкладки
            TabPaneMain.getSelectionModel().getSelectedItem().setText(selectedFile.getName());
            // Меняем статус
            LabelStatus.setText(String.format("Открыт файл %s", path));
        } // if
    }

    /** Открытие файла */
    public void openFileNoChooser(File selectedFile) {
        // Если диалог подтверждён
        if (selectedFile != null)  {
            // Создаём новый документ
            createNewDoc();
            // Получаем путь
            String path = selectedFile.getAbsolutePath();
            // Сохраняем путь по выбранному индексу
            _pathsSave.set(TabPaneMain.getSelectionModel().getSelectedIndex(), path);
            // Читаем файл
            readFile(path);
            // Меняем тайтл вкладки
            TabPaneMain.getSelectionModel().getSelectedItem().setText(selectedFile.getName());
            // Меняем статус
            LabelStatus.setText(String.format("Открыт файл %s", path));
        } // if
    }

    /** Чтение файла в кодировки юникод. Если кодировка не юникод, то с большой вероятность этот файл не текстовый */
    private void readFile(String path) {
        try (var br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"))) {

            StringBuilder text = new StringBuilder();
            String nextString;

            while ((nextString = br.readLine()) != null) {
                text.append(nextString).append("\n");
            } // while

            LabelStatus.setText(String.format("Открыт файл %s", path));
            _textAreaList.get(TabPaneMain.getSelectionModel().getSelectedIndex()).setFont(Font.font("Segoe UI", 13));
            _textAreaList.get(TabPaneMain.getSelectionModel().getSelectedIndex()).setText(text.toString());
        } catch(java.nio.charset.MalformedInputException mie) {
            System.out.println(mie.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Ошибка чтения");
            alert.setHeaderText("Данный файл не соответствует кодировке UTF-8.\nДальнейшее чтение невозможно.");
            alert.setContentText(mie.getMessage());
            alert.showAndWait();
            Logger.getLogger(TextEditorController.class.getName())
                    .log(Level.SEVERE, null, mie);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Ошибка чтения");
            alert.setHeaderText("Невозможно прочитать данный файл");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
            Logger.getLogger(TextEditorController.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /** Сохранить */
    public void save() {
        if (_pathsSave.get(TabPaneMain.getSelectionModel().getSelectedIndex()).equals("")) {
            openSaveDialog();
        } else {
            writeFile();
        }
    }

    /** Запись файла */
    private void writeFile() {
        try (var writer = new FileWriter(_pathsSave.get(TabPaneMain.getSelectionModel().getSelectedIndex()), false)) {
            writer.write(_textAreaList.get(TabPaneMain.getSelectionModel().getSelectedIndex()).getText());
            writer.flush();
            LabelStatus.setText("Сохранено");
        } catch (IOException ex) {
            ShowWriteError(ex);
        }
    }

    /** Запись файла по указанному пути */
    private void writeFile(String fileName) {
        try (var writer = new FileWriter(fileName, false)) {
            writer.write(_textAreaList.get(TabPaneMain.getSelectionModel().getSelectedIndex()).getText());
            writer.flush();
            LabelStatus.setText(String.format("Сохранён файл %s", fileName));
        } catch (IOException ex) {
            ShowWriteError(ex);
        }
    }

    /** Показать ошибку записи */
    private void ShowWriteError(IOException ex) {
        System.out.println(ex.getMessage());
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("Ошибка записи");
        alert.setHeaderText("Невозможно записать данный файл");
        alert.setContentText(ex.getMessage());
        alert.showAndWait();
        LabelStatus.setText("Ошибка записи");
    }

    /** Сохранить как */
    public void openSaveDialog() {
        // Настройка диалога
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить файл");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Все файлы", "*.*"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));
        // Показываем диалог
        File selectedFile = fileChooser.showSaveDialog(VBoxMain.getScene().getWindow());
        // Если диалог подтверждён
        if (selectedFile != null) {
            // Сохраняем путь по выбранному индексу
            _pathsSave.set(TabPaneMain.getSelectionModel().getSelectedIndex(), selectedFile.getAbsolutePath());
            // Сохраняем
            writeFile(selectedFile.getAbsolutePath());
            // Меняем тайтл выбранной вкладки
            TabPaneMain.getSelectionModel().getSelectedItem().setText(selectedFile.getName());
            // Меняем статус
            LabelStatus.setText(String.format("Сохранено как %s", selectedFile.getAbsolutePath()));
        }
    }

    /** Закрыть документ */
    public void closeDoc() {
        var index = TabPaneMain.getSelectionModel().getSelectedIndex();
        if (_anchorPaneList.size() != 1) {
            TabPaneMain.getTabs().remove(index);
            _anchorPaneList.remove(index);
            _pathsSave.remove(index);
            _tabList.remove(index);
            _textAreaList.remove(index);
            TabPaneMain.getSelectionModel().selectLast();
            LabelStatus.setText("Документ закрыт");
        } else {
            TabPaneMain.getSelectionModel().getSelectedItem().setText("Новый");
            _textAreaList.get(TabPaneMain.getSelectionModel().getSelectedIndex()).setText("");
            LabelStatus.setText("Документ закрыт");
        }
    }

    /** Выход */
    public void exit() {
        thisStage.close();
    }

    /** О программе */
    public void about() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("О программе");
        alert.setHeaderText("Простой текстовый редактор с возможностью чтения и сохранения файлов,\n" +
                "а также удобной навигацией в виде вкладок.");
        alert.setContentText("Написанный с помощью технологии Java FX.\n\nCopyright (c) Алексей Юрьевич Бурьянов, 2018.");
        alert.showAndWait();
    }
}