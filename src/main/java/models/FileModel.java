package models;

import javafx.beans.property.*;

import java.time.LocalDate;

public class FileModel {

    /** Имя файла */
    private final StringProperty name;
    /** Расширение */
    private final StringProperty extension;
    /** РаЗмер */
    private final StringProperty size;
    /** Дата создания */
    private final ObjectProperty<LocalDate> creationTime;
    /** Дата последнего изменения */
    private final ObjectProperty<LocalDate> lastModified;

    private String fullPath;

    public FileModel() {
        this(null, null, null, null, null);
    }

    public FileModel(String name, String extension, String size, LocalDate dateCreation, LocalDate lastModified) {
        this.name = new SimpleStringProperty(name);
        this.extension = new SimpleStringProperty(extension);
        this.size = new SimpleStringProperty(size);
        this.creationTime = new SimpleObjectProperty<>(dateCreation);
        this.lastModified = new SimpleObjectProperty<>(lastModified);
    }

    public String getName() {
        return name.get();
    }
    public StringProperty nameProperty() {
        return name;
    }
    public void setName(String name) {
        this.name.set(name);
    }

    public String getExtension() {
        return extension.get();
    }
    public StringProperty extensionProperty() {
        return extension;
    }
    public void setExtension(String extension) {
        this.extension.set(extension);
    }

    public String getSize() {
        return size.get();
    }
    public StringProperty sizeProperty() {
        return size;
    }
    public void setSize(String size) {
        this.size.set(size);
    }

    public LocalDate getCreationTime() {
        return creationTime.get();
    }
    public ObjectProperty<LocalDate> creationTimeProperty() {
        return creationTime;
    }
    public void setCreationTime(LocalDate creationTime) {
        this.creationTime.set(creationTime);
    }

    public LocalDate getLastModified() {
        return lastModified.get();
    }
    public ObjectProperty<LocalDate> lastModifiedProperty() {
        return lastModified;
    }
    public void setLastModified(LocalDate lastModified) {
        this.lastModified.set(lastModified);
    }

    public String getFullPath() {
        return fullPath;
    }
    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }
}