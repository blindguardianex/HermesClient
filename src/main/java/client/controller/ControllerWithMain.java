package client.controller;

/**
 * Интерфейс помечающий контроллеры, взаимодействующие с
 * контроллером главного окна
 * @see client.controller.MainControl
 */
public interface ControllerWithMain {

    public void setMainControl(MainControl mainControl);
}
