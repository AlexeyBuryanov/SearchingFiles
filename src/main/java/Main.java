import controllers.HomeController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Программа позволяет искать файлы и папки на компьютере, начиная с корневой
 * папки выбранного диска.
 * + Поиск производится в отдельном потоке.
 * + Файлы ищутся по маске.
 * + Результаты показываются в таблице (имя, расширение, размер, дата создания).
 * + Найденные файлы можно просматривать в отдельном окне (картинки и текст).
 * + Начало поиска выбирается в диалоговом окне.
 * */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        var fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("views/Home.fxml"));

        primaryStage.setScene(new Scene(fxmlLoader.load(), 700, 500));
        primaryStage.setTitle("Поиск файлов");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("icons/favicon.png")));

        ((HomeController)fxmlLoader.getController()).Init(primaryStage);

        primaryStage.show();
    }

    public static void main(String[] args) { launch(args); }
}
