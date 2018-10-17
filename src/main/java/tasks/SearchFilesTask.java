package tasks;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import models.FileModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;

public class SearchFilesTask extends Task<ObservableList<FileModel>> {

    private String typeFile;
    private String pathSearch;
    private ObservableList<FileModel> fileData = FXCollections.observableArrayList();

    public SearchFilesTask(String typeFile, String pathSearch) {
        this.typeFile = typeFile;
        this.pathSearch = pathSearch;
    }

    private void searchFiles(String pathSearch) {
        var file = new File(pathSearch);
        var files = file.listFiles();

        if (files != null) {
            var list = Arrays.asList(files);
            list.forEach(f -> {
                if (f.isFile()) {
                    var ext = f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(".")+1);
                    if (typeFile.equals("*")) {
                        parseIt(f, ext);
                    } else if (ext.equals(typeFile)) {
                        parseIt(f, ext);
                    } // if
                } else {
                    // Рекурсия
                    searchFiles(f.getAbsolutePath());
                } // if-else
            });
        } // if
    }

    private void parseIt(File f, String ext) {
        try {
            var notParsedDate1 = Files.readAttributes(f.toPath(), BasicFileAttributes.class).creationTime().toString();
            var year1 = notParsedDate1.substring(0, notParsedDate1.indexOf('-'));
            var month1 = notParsedDate1.substring(notParsedDate1.indexOf('-')+1, notParsedDate1.lastIndexOf('-'));
            var day1 = notParsedDate1.substring(notParsedDate1.lastIndexOf('-')+1, notParsedDate1.lastIndexOf('-')+3);

            var notParsedDate2 = new SimpleDateFormat("yyyy-MM-dd").format(f.lastModified());
            var year2 = notParsedDate2.substring(0, notParsedDate2.indexOf('-'));
            var month2 = notParsedDate2.substring(notParsedDate2.indexOf('-')+1, notParsedDate2.lastIndexOf('-'));
            var day2 = notParsedDate2.substring(notParsedDate2.lastIndexOf('-')+1, notParsedDate2.lastIndexOf('-')+3);

            var sizeStr = "";
            var size = f.length()/1024;
            if (size < 1024) {
                sizeStr = size + " кб";
            } else {
                sizeStr = size/1024 + " мб";
            } // if

            var model = new FileModel(f.getName().substring(0,
                    f.getName().lastIndexOf('.')),
                    ext, sizeStr,
                    LocalDate.of(Integer.parseInt(year1), Integer.parseInt(month1), Integer.parseInt(day1)),
                    LocalDate.of(Integer.parseInt(year2), Integer.parseInt(month2), Integer.parseInt(day2)));
            model.setFullPath(f.getAbsolutePath());

            if (!fileData.contains(model))
                fileData.add(model);
        } catch (IOException er) {
            er.printStackTrace();
        } // try-catch
    }

    @Override
    protected ObservableList<FileModel> call() {
        searchFiles(pathSearch);
        return fileData;
    }
}