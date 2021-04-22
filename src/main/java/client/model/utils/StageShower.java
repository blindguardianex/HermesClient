package client.model.utils;

import client.controller.ControllerWithMain;
import client.controller.ControllerWithRoot;
import client.controller.MainControl;
import client.controller.RootControl;
import client.model.ClientApp;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

/**
 * Билдер, конструирующий новые окна
 */
public class StageShower {
    private final Image ico;

    private static MainControl mainControl;
    private static RootControl rootControl;

    private FXMLLoader loader;
    private Pane pane;

    private boolean customCloseRequest = false;
    private EventHandler<WindowEvent> event;

    private boolean alwaysTop = false;

    private StageShower(String resource) throws IOException {
        ico = new Image(getClass().getResource("/Full-logo.jpg").toExternalForm());
        loader = new FXMLLoader();
        loader.setLocation(ClientApp.class.getResource(resource));
        pane = loader.load();
    }

    public static StageShower getShower(String resource) throws IOException {
        return new StageShower(resource);
    }

    /**
     * Вызывается для показа сконструированного окна
     */
    public void show(){
        Scene scene = new Scene(pane);

        Stage stage = new Stage();
        if (customCloseRequest) stage.setOnCloseRequest(event);
        stage.setAlwaysOnTop(alwaysTop);
        stage.getIcons().add(ico);

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Предустанавривает онтроллер главного окна в контроллер созданного окна
     * @see MainControl
     * @return
     */
    public StageShower presetMainControl(){
        if (loader.getController() instanceof ControllerWithMain){
            ControllerWithMain controller = loader.getController();
            controller.setMainControl(mainControl);
            return this;
        }
        throw new UnsupportedOperationException("This controller not supported main controller");
    }

    /**
     * Предустанавривает онтроллер слоя-оболочки в контроллер созданного окна
     * @see RootControl
     * @return
     */
    public StageShower presetRootControl(){
        if (loader.getController() instanceof ControllerWithRoot){
            ControllerWithRoot controller = loader.getController();
            controller.setRootControl(rootControl);
            return this;
        }
        throw new UnsupportedOperationException("This controller not supported root controller");
    }

    /**
     * Устанавливает событие закрытия приложения на кнопку закрытия окна
     * @param event
     * @return
     */
    public StageShower withOnCloseRequest(EventHandler<WindowEvent> event){
        this.event = event;
        customCloseRequest=true;
        return this;
    }

    /**
     * Устанавливает окно всегда поверх других окон
     * @return
     */
    public StageShower alwaysTop(){
        alwaysTop=true;
        return this;
    }

    /**
     * Возвращает контроллер созданного окна
     * @param <T>
     * @return
     */
    public<T> T getController(){
        return loader.getController();
    }

    public static void setMainControl(MainControl mainControl) {
        StageShower.mainControl = mainControl;
    }

    public static void setRootControl(RootControl rootControl) {
        StageShower.rootControl = rootControl;
    }
}
