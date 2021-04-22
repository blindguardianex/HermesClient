package client.controller;

/**
 * Интерфейс, помечающий контроллеры, взаимодействующие с
 * контроллером главного слоя
 * @see client.controller.RootControl
 */
public interface ControllerWithRoot {

    public void setRootControl(RootControl rootControl);
}
